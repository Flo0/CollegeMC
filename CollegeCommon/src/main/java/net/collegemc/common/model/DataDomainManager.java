package net.collegemc.common.model;

import net.collegemc.common.GlobalGateway;
import net.collegemc.common.bridge.RemoteEventManager;
import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.utils.TypeInferredSerializedUnit;
import org.redisson.api.listener.MessageListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class DataDomainManager implements MessageListener<AutoSyncUpdateEvent<?, ?>> {

  private final Map<ContextKey<?, ?>, AutoSynchronizedGlobalDataMap<?, ?>> autoSyncDomainMap = new ConcurrentHashMap<>();
  private final Map<ContextKey<?, ?>, LocalDataMap<?, ?>> localDataDomainMap = new ConcurrentHashMap<>();
  private final Map<ContextKey<?, ?>, GlobalDataMap<?, ?>> globalDataDomainMap = new ConcurrentHashMap<>();
  private final Map<ContextKey<String, ?>, GlobalDataObject<?>> globalDataObjectMap = new ConcurrentHashMap<>();

  public DataDomainManager(RemoteEventManager remoteEventManager) {
    remoteEventManager.registerListener("_AUTO_SYNC_", AutoSyncUpdateEvent.class, this);
  }

  public <K, V> LocalDataMap<K, V> getOrCreateLocalDomain(DataMapContext<K, V> key) {
    return (LocalDataMap<K, V>) localDataDomainMap.computeIfAbsent(key, (k) -> new LocalDataMap<>(key));
  }

  public <K, V> GlobalDataMap<K, V> getOrCreateGlobalDomain(DataMapContext<K, V> key) {
    return (GlobalDataMap<K, V>) globalDataDomainMap.computeIfAbsent(key, (k) -> new GlobalDataMap<>(key));
  }

  public <T> GlobalDataObject<T> getOrCreateGlobalObject(DataObjectContext<T> key) {
    return (GlobalDataObject<T>) globalDataObjectMap.computeIfAbsent(key, (k) -> new GlobalDataObject<>(key));
  }

  public <K, V> AutoSynchronizedGlobalDataMap<K, V> getOrCreateAutoSyncDataDomain(DataMapContext<K, V> key) {
    return (AutoSynchronizedGlobalDataMap<K, V>) autoSyncDomainMap.computeIfAbsent(key, (k) -> new AutoSynchronizedGlobalDataMap<>(key));
  }

  public <T> GlobalDataObject<T> getOrCreateGlobalObject(T defaultObject, GsonSerializer gsonSerializer, String database, String namespace) {
    DataObjectContext<T> key = DataObjectContext.<T>objectContextBuilder()
            .defaultObj(defaultObject)
            .serializer(gsonSerializer)
            .valueClass((Class<T>) defaultObject.getClass())
            .redissonClient(GlobalGateway.getRedissonClient())
            .mongoDatabase(GlobalGateway.getMongoClient().getDatabase(database))
            .namespace(namespace)
            .build();

    return (GlobalDataObject<T>) globalDataObjectMap.computeIfAbsent(key, (k) -> new GlobalDataObject<>(key));
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void onMessage(CharSequence charSequence, AutoSyncUpdateEvent updateEvent) {
    AutoSynchronizedGlobalDataMap syncMap = autoSyncDomainMap.get(updateEvent.getContextKey());

    if(syncMap == null) {
      return;
    }

    TypeInferredSerializedUnit<?> unit = updateEvent.getUpdatedKey();
    Object key = unit.translate(syncMap.getSerializer());

    if (syncMap.isLocallyCached(key)) {
      syncMap.triggerLocalCacheRenew(key);
    }
  }
}