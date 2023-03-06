package net.collegemc.common.model;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GlobalDataObject<T> {

  private static final String OBJECT_PREFIX = "__DATA_OBJECT::";

  private final GlobalDataMap<String, T> mapBackbone;
  private final String objectKey;

  public GlobalDataObject(DataObjectContext<T> objectContext) {
    this.mapBackbone = new GlobalDataMap<>(objectContext);
    this.objectKey = OBJECT_PREFIX + objectContext.getCreator().apply(null).getClass().getName();
  }


  public void enableLocalCache() {
    mapBackbone.enableLocalCacheFor(objectKey);
  }

  public CompletableFuture<Void> enableLocalCacheAsync() {
    return mapBackbone.enableLocalCacheAsyncFor(objectKey);
  }

  public void disableLocalCache() {
    mapBackbone.disableLocalCacheFor(objectKey);
  }

  public T getCachedValue() {
    return mapBackbone.getCachedValue(objectKey);
  }

  public void triggerLocalCacheRenew() {
    mapBackbone.triggerLocalCacheRenew(objectKey);
  }

  public boolean isLocallyCached() {
    return mapBackbone.isLocallyCached(objectKey);
  }

  public T getOrCreateRealTimeData() {
    return mapBackbone.getOrCreateRealTimeData(objectKey);
  }

  public CompletableFuture<Void> applyAsync(Consumer<T> consumer) {
    return mapBackbone.forEachAsync((k, v) -> consumer.accept(v));
  }

  public synchronized void apply(Consumer<T> consumer) {
    mapBackbone.forEach((k, v) -> consumer.accept(v));
  }

  public CompletableFuture<T> getOrCreateRealTimeDataAsync() {
    return mapBackbone.getOrCreateRealTimeDataAsync(objectKey);
  }

  public void delete() {
    mapBackbone.deleteDataGloballySync(objectKey);
  }

  public CompletableFuture<Void> deleteAsync() {
    return mapBackbone.deleteDataGloballyAsync(objectKey);
  }

}
