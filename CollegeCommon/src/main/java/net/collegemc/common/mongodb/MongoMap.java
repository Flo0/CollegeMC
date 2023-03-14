package net.collegemc.common.mongodb;

import com.google.common.base.Preconditions;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import net.collegemc.common.gson.GsonSerializer;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MongoMap<K, V> implements Map<K, V> {

  private final MongoCollection<V> mongoBackbone;
  private final Class<K> keyClass;

  public MongoMap(MongoCollection<V> mongoBackbone, GsonSerializer gsonSerializer, Class<K> keyClass) {
    Preconditions.checkArgument(mongoBackbone != null && gsonSerializer != null);
    this.keyClass = keyClass;

    CodecRegistry codec = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), gsonSerializer.createCodecRegistry());
    this.mongoBackbone = mongoBackbone.withCodecRegistry(codec);
  }

  @Override
  public int size() {
    return (int) this.mongoBackbone.countDocuments();
  }

  @Override
  public boolean isEmpty() {
    return this.size() == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    if (!this.mongoBackbone.getDocumentClass().isInstance(key)) {
      return false;
    }

    return this.mongoBackbone.countDocuments(Filters.eq(key)) > 0;
  }

  @Override
  public boolean containsValue(Object value) {
    try (MongoCursor<V> cursor = this.mongoBackbone.find().iterator()) {
      while (cursor.hasNext()) {
        if (cursor.next().equals(value)) {
          return true;
        }
      }
    }
    return false;
  }


  @Nullable
  @Contract("null -> null")
  @Override
  public V get(Object key) {
    if (!this.keyClass.isInstance(key)) {
      return null;
    }
    return this.mongoBackbone.find(Filters.eq(key)).first();
  }

  @Nullable
  @Override
  public V put(@NotNull K key, @NotNull V value) {
    ReplaceOptions options = new ReplaceOptions().upsert(true);
    V replaced = this.get(key);
    this.mongoBackbone.replaceOne(Filters.eq(key), value, options);
    return replaced;
  }

  public void fastPut(@NotNull K key, @NotNull V value) {
    ReplaceOptions options = new ReplaceOptions().upsert(true);
    this.mongoBackbone.replaceOne(Filters.eq(key), value, options);
  }

  @Nullable
  @Override
  public V remove(@NotNull Object key) {
    V replaced = this.get(key);
    this.mongoBackbone.deleteOne(Filters.eq(key));
    return replaced;
  }

  public void fastRemove(@NotNull Object key) {
    this.mongoBackbone.deleteOne(Filters.eq(key));
  }

  @Override
  public void putAll(@NotNull Map<? extends K, ? extends V> map) {
    map.forEach(this::fastPut);
  }

  @Override
  public void clear() {
    this.mongoBackbone.drop();
  }

  @Override
  public Set<K> keySet() {
    Set<K> keys = new HashSet<>();
    this.mongoBackbone.distinct("_id", this.keyClass).into(keys);
    return keys;
  }

  @NotNull
  @Override
  public Collection<V> values() {
    List<V> values = new ArrayList<>();
    this.mongoBackbone.find().into(values);
    return values;
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    Map<K, V> map = new HashMap<>();
    for (K key : this.keySet()) {
      map.put(key, this.get(key));
    }
    return map.entrySet();
  }

  @NotNull
  public <E> List<V> findByProperty(@NotNull String property, E value) {
    return this.query(coll -> coll.find(Filters.eq(property, value)), iter -> {
      List<V> values = new ArrayList<>();
      iter.into(values);
      return values;
    });
  }

  @NotNull
  public List<V> queryToplist(@NotNull String property, int limit, boolean ascending) {
    Bson sort = ascending ? Sorts.ascending(property) : Sorts.descending(property);
    return this.query(MongoCollection::find, iter -> {
      List<V> values = new ArrayList<>();
      iter.sort(sort).limit(limit).into(values);
      return values;
    });
  }

  @NotNull
  public <I, R> R query(@NotNull Function<MongoCollection<V>, I> queryFunction, @NotNull Function<I, R> resultFunction) {
    return resultFunction.apply(queryFunction.apply(this.mongoBackbone));
  }
}
