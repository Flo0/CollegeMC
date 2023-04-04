package net.collegemc.common.model;

import com.google.common.base.Preconditions;
import net.collegemc.common.database.mongodb.MongoMap;
import org.redisson.api.MapOptions;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LocalDataMap<K, V> {

  private final DataMapContext<K, V> dataMapContext;
  private final Map<K, V> localMap = new ConcurrentHashMap<>();
  private final RMapCache<K, V> redisAccess;
  private final MongoMap<K, V> mongoStorage;

  protected LocalDataMap(DataMapContext<K, V> dataMapContext) {
    this.dataMapContext = dataMapContext;
    this.mongoStorage = dataMapContext.createMapper();

    RedissonClient redissonClient = dataMapContext.getRedissonClient();
    if (redissonClient != null) {
      MongoBackedLoader<K, V> loader = new MongoBackedLoader<>(mongoStorage);
      MapOptions<K, V> options = MapOptions.<K, V>defaults()
              .loader(loader)
              .writer(loader)
              .writeMode(MapOptions.WriteMode.WRITE_THROUGH);
      this.redisAccess = redissonClient.getMapCache(dataMapContext.getNamespace(), options);
    } else {
      this.redisAccess = null;
    }
  }

  public MongoMap<K, V> getBackbone() {
    return mongoStorage;
  }

  public List<V> getAllLoadedValues() {
    return List.copyOf(this.localMap.values());
  }

  public List<V> getLocalTop(Comparator<V> comparator, int limit) {
    return this.localMap.values().stream().sorted(comparator).limit(limit).toList();
  }

  public Optional<V> findInLocalCache(Predicate<V> predicate) {
    return this.localMap.values().stream().filter(predicate).findAny();
  }

  public void loadAllValuesIntoLocalCache() {
    this.mongoStorage.keySet().forEach(this::loadDataSync);
  }

  public List<K> getAllLoadedKeys() {
    return List.copyOf(this.localMap.keySet());
  }

  public void forEachLocal(BiConsumer<K, V> consumer) {
    this.localMap.forEach(consumer);
  }

  public boolean isDataLoaded(K key) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    return this.localMap.containsKey(key);
  }

  public boolean exists(K key) {
    if (key == null) {
      return false;
    }
    if (this.isDataLoaded(key)) {
      return true;
    }
    if (this.redisAccess != null && this.redisAccess.containsKey(key)) {
      return true;
    }
    return this.mongoStorage.containsKey(key);
  }

  public CompletableFuture<Boolean> existsAsync(K key) {
    return CompletableFuture.supplyAsync(() -> this.exists(key));
  }

  public V loadAndGetSync(K key) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    this.loadDataSync(key);
    return this.getData(key);
  }

  public CompletableFuture<V> loadAndGetAsync(K key) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    return CompletableFuture.supplyAsync(() -> this.loadAndGetSync(key));
  }

  public void loadDataSync(K key) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    if (this.localMap.containsKey(key)) {
      throw new IllegalStateException("Tried to load data for already loaded key.");
    }
    V value;
    if (this.redisAccess == null) {
      value = this.mongoStorage.get(key);
      if (value == null) {
        value = this.dataMapContext.getCreator().apply(key);
        this.mongoStorage.put(key, value);
      }
    } else {
      value = this.redisAccess.get(key);
      if (value == null) {
        value = this.dataMapContext.getCreator().apply(key);
        this.redisAccess.fastPut(key, value);
      }
    }
    if (value == null) {
      throw new IllegalStateException("Null insertion in local domain is not permitted.");
    }
    this.localMap.put(key, value);
  }

  public CompletableFuture<Void> loadDataAsync(K key) {
    return CompletableFuture.runAsync(() -> this.loadDataSync(key));
  }

  public synchronized void deleteDataGloballySync(K key) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    this.localMap.remove(key);
    if (this.redisAccess != null) {
      this.redisAccess.remove(key);
    }
    this.mongoStorage.remove(key);
  }

  public CompletableFuture<Void> deleteDataGloballyAsync(K key) {
    return CompletableFuture.runAsync(() -> this.deleteDataGloballySync(key));
  }

  public synchronized void unloadDataSync(K key) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    Preconditions.checkState(this.localMap.containsKey(key), "Tried to unload data for absent key.");
    V value = this.localMap.remove(key);
    if (this.redisAccess == null) {
      this.mongoStorage.put(key, value);
    } else {
      this.redisAccess.fastPut(key, value, 15, TimeUnit.MINUTES);
    }
  }

  public CompletableFuture<Void> unloadDataAsync(K key) {
    return CompletableFuture.runAsync(() -> this.unloadDataSync(key));
  }

  public V getData(K key) {
    Preconditions.checkArgument(key != null, "Key cant be null.");
    return Optional.ofNullable(this.localMap.get(key)).orElseThrow(LocalDataMap::unloadedState);
  }

  // This might cause concurrency problems...
  public CompletableFuture<Void> applyToDataAsync(K key, Consumer<V> consumer) {
    if (this.isDataLoaded(key)) {
      consumer.accept(this.getData(key));
      return CompletableFuture.completedFuture(null);
    } else {
      return this.loadAndGetAsync(key).thenAccept(consumer).thenRun(() -> this.unloadDataSync(key));
    }
  }

  private static IllegalStateException unloadedState() {
    return new IllegalStateException("Tried to get data for unloaded key.");
  }

  public synchronized void saveCacheSync() {
    if (this.redisAccess == null) {
      mongoStorage.putAll(this.localMap);
    } else {
      this.localMap.forEach(this.redisAccess::fastPut);
    }
  }

  public CompletableFuture<Void> saveCacheAsync() {
    return CompletableFuture.runAsync(this::saveCacheSync);
  }


}
