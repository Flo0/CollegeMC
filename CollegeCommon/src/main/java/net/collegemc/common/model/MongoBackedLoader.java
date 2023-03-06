package net.collegemc.common.model;

import lombok.RequiredArgsConstructor;
import net.collegemc.common.mongodb.MongoMap;
import org.redisson.api.map.MapLoader;
import org.redisson.api.map.MapWriter;

import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class MongoBackedLoader<K, V> implements MapLoader<K, V>, MapWriter<K, V> {

  private final MongoMap<K, V> mongoMap;

  @Override
  public V load(K k) {
    return mongoMap.get(k);
  }

  @Override
  public Iterable<K> loadAllKeys() {
    return mongoMap.keySet();
  }

  @Override
  public void write(Map<K, V> map) {
    mongoMap.putAll(map);
  }

  @Override
  public void delete(Collection<K> collection) {
    collection.forEach(mongoMap::remove);
  }
}
