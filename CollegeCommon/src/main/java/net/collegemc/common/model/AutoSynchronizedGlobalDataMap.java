package net.collegemc.common.model;

import lombok.AccessLevel;
import lombok.Getter;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.utils.TypeInferredSerializedUnit;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class AutoSynchronizedGlobalDataMap<K, V> extends GlobalDataMap<K, V> {

  private final ContextKey<K, V> contextKey;
  @Getter(AccessLevel.PROTECTED)
  private final GsonSerializer serializer;

  protected AutoSynchronizedGlobalDataMap(DataMapContext<K, V> dataMapContext) {
    super(dataMapContext);
    this.serializer = dataMapContext.getGsonSerializer();
    this.contextKey = dataMapContext.asKey();
  }

  private void fireUpdate(K key) {
    String json = serializer.toJson(key);
    TypeInferredSerializedUnit<K> typeInferredSerializedUnit = new TypeInferredSerializedUnit<>(contextKey.keyClass, json);
    AutoSyncUpdateEvent<K, V> event = new AutoSyncUpdateEvent<>(contextKey, typeInferredSerializedUnit);
    GlobalGateway.getRemoteEventManager().callEvent("_AUTO_SYNC_", event);
  }

  @Override
  public V getOrCreateRealTimeData(K key) {
    V realTimeData = super.getOrCreateRealTimeData(key);
    fireUpdate(key);
    return realTimeData;
  }

  @Override
  public synchronized void forEach(BiConsumer<K, V> consumer) {
    super.forEach(consumer.andThen((k, v) -> fireUpdate(k)));
  }

  @Override
  public void batchDeleteDataGloballySync(Collection<K> keys) {
    super.batchDeleteDataGloballySync(keys);
  }

  @Override
  public void applyToBoth(K keyOne, K keyTwo, BiConsumer<V, V> action) {
    super.applyToBoth(keyOne, keyTwo, action);
    fireUpdate(keyOne);
    fireUpdate(keyTwo);
  }

  @Override
  public void applyToAll(Collection<K> keys, Consumer<V> action) {
    super.applyToAll(keys, action);
    keys.forEach(this::fireUpdate);
  }

  @Override
  public void applyToData(K key, Consumer<V> action) {
    super.applyToData(key, action);
    fireUpdate(key);
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
    fireUpdate(keyOne);
    fireUpdate(keyTwo);
    return value;
  }

  @Override
  public <T> T compute(K key, Function<V, T> computation) {
    T value = super.compute(key, computation);
    fireUpdate(key);
    return value;
  }
}
