package net.collegemc.common.model;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.utils.TypeInferredSerializedUnit;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class AutoSynchronizedGlobalDataMap<K, V> extends GlobalDataMap<K, V> {

  private final ContextKey<K, V> contextKey;
  @Getter(AccessLevel.PROTECTED)
  private final GsonSerializer serializer;
  private final Map<String, BiConsumer<K, V>> listeners = new ConcurrentHashMap<>();

  protected AutoSynchronizedGlobalDataMap(DataMapContext<K, V> dataMapContext) {
    super(dataMapContext);
    this.serializer = dataMapContext.getGsonSerializer();
    this.contextKey = dataMapContext.asKey();
  }

  private void fireUpdate(K key) {
    String json = this.serializer.toJson(key);
    TypeInferredSerializedUnit<K> typeInferredSerializedUnit = new TypeInferredSerializedUnit<>(this.contextKey.keyClass, json);
    AutoSyncUpdateEvent<K, V> event = new AutoSyncUpdateEvent<>(this.contextKey, typeInferredSerializedUnit);
    GlobalGateway.getRemoteEventManager().callEvent("_AUTO_SYNC_", event);
  }

  public void registerListener(String key, BiConsumer<K, V> listener) {
    Preconditions.checkArgument(!this.listeners.containsKey(key), "Listener already registered for key " + key);
    this.listeners.put(key, listener);
  }

  public void unregisterListener(String key) {
    Preconditions.checkArgument(this.listeners.containsKey(key), "Listener not registered for key " + key);
    this.listeners.remove(key);
  }

  @Override
  public void triggerLocalCacheRenew(K key) {
    super.triggerLocalCacheRenew(key);
    listeners.values().forEach(listener -> listener.accept(key, this.getCachedValue(key)));
  }

  @Override
  public synchronized void forEach(BiConsumer<K, V> consumer) {
    super.forEach(consumer.andThen((k, v) -> this.fireUpdate(k)));
  }

  @Override
  public void batchDeleteDataGloballySync(Collection<K> keys) {
    super.batchDeleteDataGloballySync(keys);
  }

  @Override
  public void applyToBoth(K keyOne, K keyTwo, BiConsumer<V, V> action) {
    super.applyToBoth(keyOne, keyTwo, action);
    this.fireUpdate(keyOne);
    this.fireUpdate(keyTwo);
  }

  @Override
  public void applyToAll(Collection<K> keys, Consumer<V> action) {
    super.applyToAll(keys, action);
    keys.forEach(this::fireUpdate);
  }

  @Override
  public void applyToData(K key, Consumer<V> action) {
    super.applyToData(key, action);
    this.fireUpdate(key);
  }

  @Override
  public <T> T batchCompute(Collection<K> keys, Function<Collection<V>, T> computation) {
    T value = super.batchCompute(keys, computation);
    keys.forEach(this::fireUpdate);
    return value;
  }

  @Override
  public <T> T biCompute(K keyOne, K keyTwo, BiFunction<V, V, T> computation) {
    T value = super.biCompute(keyOne, keyTwo, computation);
    this.fireUpdate(keyOne);
    this.fireUpdate(keyTwo);
    return value;
  }

  @Override
  public <T> T compute(K key, Function<V, T> computation) {
    T value = super.compute(key, computation);
    this.fireUpdate(key);
    return value;
  }
}
