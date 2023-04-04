package net.collegemc.common.model;

import com.google.common.base.Preconditions;
import net.collegemc.common.database.mongodb.MongoMap;
import net.collegemc.common.utils.Pair;
import org.redisson.api.MapOptions;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class GlobalDataMap<K, V> {

  private static final String LOCK_PREFIX = "LOCK_";

  private final DataMapContext<K, V> dataMapContext;
  private final RMapCache<K, V> redisAccess;
  private final MongoMap<K, V> mongoStorage;
  private final RedissonClient redissonClient;
  private final Map<K, V> localCache = new ConcurrentHashMap<>();

  protected GlobalDataMap(DataMapContext<K, V> dataMapContext) {
    this.dataMapContext = dataMapContext;
    this.redissonClient = dataMapContext.getRedissonClient();
    this.mongoStorage = dataMapContext.createMapper();
    MongoBackedLoader<K, V> loader = new MongoBackedLoader<>(this.mongoStorage);
    MapOptions<K, V> options = MapOptions.<K, V>defaults()
            .loader(loader)
            .writer(loader)
            .writeMode(MapOptions.WriteMode.WRITE_THROUGH);
    this.redisAccess = dataMapContext.getRedissonClient().getMapCache(dataMapContext.getNamespace(), options);
  }

  public MongoMap<K, V> getBackbone() {
    return this.mongoStorage;
  }

  public List<K> getCachedKeys() {
    return List.copyOf(this.localCache.keySet());
  }

  public void enableLocalCacheFor(K key) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    Preconditions.checkState(!this.isLocallyCached(key), "Cant enable cache twice: " + key);
    this.localCache.put(key, this.getOrCreateRealTimeData(key));
  }

  public CompletableFuture<Void> enableLocalCacheAsyncFor(K key) {
    return CompletableFuture.runAsync(() -> this.enableLocalCacheFor(key));
  }

  public void disableLocalCacheFor(K key) {
    this.localCache.remove(key);
  }

  public V getCachedValue(K key) {
    return this.localCache.get(key);
  }

  public void triggerLocalCacheRenew(K key) {
    Preconditions.checkState(this.isLocallyCached(key), "Tried to renew cached value for uncached key: " + key);
    this.localCache.put(key, this.getOrCreateRealTimeData(key));
  }

  public boolean isLocallyCached(K key) {
    return this.localCache.containsKey(key);
  }

  public V getOrCreateRealTimeData(K key) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    AtomicReference<V> atomic = new AtomicReference<>();
    this.withRLock(key, () -> {
      V value = this.redisAccess.get(key);
      if (value == null) {
        value = this.dataMapContext.getCreator().apply(key);
        this.redisAccess.fastPut(key, value, 30, TimeUnit.MINUTES);
      }
      atomic.set(value);
    });
    return atomic.get();
  }

  public CompletableFuture<Void> forEachAsync(BiConsumer<K, V> consumer) {
    return CompletableFuture.runAsync(() -> this.forEach(consumer));
  }

  public synchronized void forEach(BiConsumer<K, V> consumer) {
    for (K key : this.fetchAllKeys()) {
      this.applyToData(key, data -> consumer.accept(key, data));
    }
  }

  public synchronized Set<K> fetchAllKeys() {
    return this.mongoStorage.keySet();
  }

  public boolean exists(K key) {
    return key != null && (this.redisAccess.containsKey(key) || this.mongoStorage.containsKey(key));
  }

  public CompletableFuture<Boolean> existsAsync(K key) {
    return CompletableFuture.supplyAsync(() -> this.exists(key));
  }

  public CompletableFuture<V> getOrCreateRealTimeDataAsync(K key) {
    return CompletableFuture.supplyAsync(() -> this.getOrCreateRealTimeData(key));
  }

  public void batchDeleteDataGloballySync(Collection<K> keys) {
    for (K key : keys) {
      if (this.isLocallyCached(key)) {
        this.disableLocalCacheFor(key);
      }
    }
    this.withAllLocks(keys, () -> keys.forEach(key -> {
      Preconditions.checkArgument(key != null, "Key cant be null.");
      this.redisAccess.remove(key);
      this.mongoStorage.remove(key);
    }));
  }

  public CompletableFuture<Void> batchDeleteDataGloballyAsync(Collection<K> keys) {
    return CompletableFuture.runAsync(() -> this.batchDeleteDataGloballySync(keys));
  }

  public void deleteDataGloballySync(K key) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    if (this.isLocallyCached(key)) {
      this.disableLocalCacheFor(key);
      this.enableLocalCacheFor(key);
    }
    this.withRLock(key, () -> {
      this.redisAccess.remove(key);
      this.mongoStorage.remove(key);
    });
  }

  public CompletableFuture<Void> deleteDataGloballyAsync(K key) {
    return CompletableFuture.runAsync(() -> this.deleteDataGloballySync(key));
  }

  public void applyToBoth(K keyOne, K keyTwo, BiConsumer<V, V> action) {
    Preconditions.checkArgument(keyOne != null && keyTwo != null, "Key cant be null.");
    this.withAllLocks(List.of(keyOne, keyTwo), () -> {
      V valueOne = this.redisAccess.getOrDefault(keyOne, this.dataMapContext.getCreator().apply(keyOne));
      V valueTwo = this.redisAccess.getOrDefault(keyTwo, this.dataMapContext.getCreator().apply(keyTwo));
      action.accept(valueOne, valueTwo);
      this.redisAccess.fastPut(keyOne, valueOne, 30, TimeUnit.MINUTES);
      this.redisAccess.fastPut(keyTwo, valueTwo, 30, TimeUnit.MINUTES);
    });
  }

  public void applyToAll(Collection<K> keys, Consumer<V> action) {
    this.withAllLocks(keys, () -> keys.stream()
            .filter(Objects::nonNull)
            .map(key -> Pair.of(key, this.redisAccess.getOrDefault(key, this.dataMapContext.getCreator().apply(key))))
            .peek(pair -> action.accept(pair.value()))
            .forEach(pair -> this.redisAccess.fastPut(pair.key(), pair.value(), 30, TimeUnit.MINUTES)));
  }

  public void applyToData(K key, Consumer<V> action) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    this.withRLock(key, () -> {
      V value = this.redisAccess.getOrDefault(key, this.dataMapContext.getCreator().apply(key));
      action.accept(value);
      this.redisAccess.fastPut(key, value, 30, TimeUnit.MINUTES);
    });
  }

  public CompletableFuture<Void> applyToDataAsync(K key, Consumer<V> action) {
    return CompletableFuture.runAsync(() -> this.applyToData(key, action));
  }

  public <T> T batchCompute(Collection<K> keys, Function<Collection<V>, T> computation) {
    return this.withAllLocksCallback(keys, () -> {
      Map<K, V> map = new HashMap<>();
      for (K key : keys) {
        Preconditions.checkArgument(key != null, "Key cant be null.");
        map.put(key, this.redisAccess.getOrDefault(key, this.dataMapContext.getCreator().apply(key)));
      }
      T computed = computation.apply(map.values());
      map.forEach((key, value) -> this.redisAccess.fastPut(key, value, 30, TimeUnit.MINUTES));
      return computed;
    });
  }

  public <T> CompletableFuture<T> biComputeAsync(K keyOne, K keyTwo, BiFunction<V, V, T> computation) {
    return CompletableFuture.supplyAsync(() -> this.biCompute(keyOne, keyTwo, computation));
  }

  public <T> T biCompute(K keyOne, K keyTwo, BiFunction<V, V, T> computation) {
    Preconditions.checkArgument(keyOne != null && keyTwo != null, "Key cant be null.");
    return this.withAllLocksCallback(List.of(keyOne, keyTwo), () -> {
      V valueOne = this.redisAccess.getOrDefault(keyOne, this.dataMapContext.getCreator().apply(keyOne));
      V valueTwo = this.redisAccess.getOrDefault(keyTwo, this.dataMapContext.getCreator().apply(keyTwo));
      T computed = computation.apply(valueOne, valueTwo);
      this.redisAccess.fastPut(keyOne, valueOne, 30, TimeUnit.MINUTES);
      this.redisAccess.fastPut(keyTwo, valueTwo, 30, TimeUnit.MINUTES);
      return computed;
    });
  }

  public <T> CompletableFuture<T> computeAsync(K key, Function<V, T> computation) {
    return CompletableFuture.supplyAsync(() -> this.compute(key, computation));
  }

  public <T> T compute(K key, Function<V, T> computation) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    return this.withRLockCallback(key, () -> {
      V value = this.redisAccess.getOrDefault(key, this.dataMapContext.getCreator().apply(key));
      T computed = computation.apply(value);
      this.redisAccess.fastPut(key, value, 30, TimeUnit.MINUTES);
      return computed;
    });
  }

  private <T> T withRLockCallback(K key, Supplier<T> supplier) {
    RLock lock = this.dataMapContext.getRedissonClient().getLock(LOCK_PREFIX + this.dataMapContext.namespace + key);
    T value = null;
    try {
      lock.lock(30, TimeUnit.SECONDS);
      value = supplier.get();
    } catch (Exception exception) {
      exception.printStackTrace();
    } finally {
      lock.unlock();
    }
    return value;
  }

  private <T> T withAllLocksCallback(Collection<K> keys, Supplier<T> supplier) {
    Collection<RLock> locks = keys.stream().map(key -> this.redissonClient.getLock(LOCK_PREFIX + this.dataMapContext.namespace + key)).toList();
    T value = null;
    try {
      locks.forEach(RLock::lock);
      value = supplier.get();
    } catch (Exception exception) {
      exception.printStackTrace();
    } finally {
      locks.forEach(RLock::unlock);
    }
    return value;
  }

  private void withRLock(K key, Runnable runnable) {
    RLock lock = this.dataMapContext.getRedissonClient().getLock(LOCK_PREFIX + this.dataMapContext.namespace + key);
    try {
      lock.lock(30, TimeUnit.SECONDS);
      runnable.run();
    } catch (Exception exception) {
      exception.printStackTrace();
    } finally {
      lock.unlock();
    }
  }

  private void withAllLocks(Collection<K> keys, Runnable runnable) {
    Collection<RLock> locks = keys.stream().map(key -> this.redissonClient.getLock(LOCK_PREFIX + this.dataMapContext.namespace + key)).toList();
    try {
      locks.forEach(RLock::lock);
      runnable.run();
    } catch (Exception exception) {
      exception.printStackTrace();
    } finally {
      locks.forEach(RLock::unlock);
    }
  }
}
