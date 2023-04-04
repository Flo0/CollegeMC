package net.collegemc.mc.libs.tasks;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.collegemc.common.database.mongodb.MongoMap;
import net.collegemc.mc.libs.CollegeLibrary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MongoBackedMap<K, V> implements Map<K, V> {

  private final Map<K, V> localMap;
  private final MongoMap<K, V> remoteMap;

  public MongoBackedMap(Map<K, V> localMap, String namespace, Class<K> keyClass, Class<V> valueClass) {
    MongoDatabase database = CollegeLibrary.getServerDatabase();
    MongoCollection<V> collection = database.getCollection(namespace, valueClass);
    this.localMap = localMap;
    this.remoteMap = new MongoMap<>(collection, CollegeLibrary.getGsonSerializer(), keyClass);
  }

  @Override
  public int size() {
    return this.localMap.size();
  }

  @Override
  public boolean isEmpty() {
    return this.localMap.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return this.localMap.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return this.localMap.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return this.localMap.get(key);
  }

  @Nullable
  @Override
  public V put(K key, V value) {
    V replaced = this.localMap.put(key, value);
    TaskManager.runOnIOPool(() -> this.remoteMap.put(key, value));
    return replaced;
  }

  public V putLocal(K key, V value) {
    return this.localMap.put(key, value);
  }

  @Override
  public V remove(Object key) {
    V removed = this.localMap.remove(key);
    TaskManager.runOnIOPool(() -> this.remoteMap.remove(removed));
    return removed;
  }

  @Override
  public void putAll(@NotNull Map<? extends K, ? extends V> m) {
    this.localMap.putAll(m);
    TaskManager.runOnIOPool(() -> this.remoteMap.putAll(m));
  }

  @Override
  public void clear() {
    this.localMap.clear();
    TaskManager.runOnIOPool(this.remoteMap::clear);
  }

  @NotNull
  @Override
  public Set<K> keySet() {
    return this.localMap.keySet();
  }

  @NotNull
  @Override
  public Collection<V> values() {
    return this.localMap.values();
  }

  @NotNull
  @Override
  public Set<Entry<K, V>> entrySet() {
    return this.localMap.entrySet();
  }

  public void loadDataFromRemote() {
    this.localMap.putAll(this.remoteMap);
  }

  public void saveDataToRemote() {
    this.remoteMap.putAll(this.localMap);
  }
}
