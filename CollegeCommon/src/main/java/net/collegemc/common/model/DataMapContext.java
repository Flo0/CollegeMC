package net.collegemc.common.model;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.mongodb.MongoMap;
import org.redisson.api.RedissonClient;

import java.util.function.Function;

@Getter
public class DataMapContext<K, V> extends ContextKey<K, V> {

  public static <K, V> MapContextBuilder<K, V> mapContextBuilder() {
    return new MapContextBuilder<>();
  }

  private final RedissonClient redissonClient;
  private final Function<K, V> creator;
  private final MongoDatabase mongoDatabase;
  private final GsonSerializer gsonSerializer;

  public DataMapContext(
          String namespace,
          Class<K> keyClass,
          Class<V> valueClass,
          RedissonClient redissonClient,
          Function<K, V> creator,
          MongoDatabase mongoDatabase,
          GsonSerializer gsonSerializer) {
    super(namespace, keyClass, valueClass);
    this.redissonClient = redissonClient;
    this.creator = creator;
    this.mongoDatabase = mongoDatabase;
    this.gsonSerializer = gsonSerializer;
  }

  public MongoCollection<V> getBackingCollection() {
    return this.mongoDatabase.getCollection(this.namespace, this.valueClass);
  }

  public MongoMap<K, V> createMapper() {
    return new MongoMap<>(this.getBackingCollection(), this.gsonSerializer, this.keyClass);
  }

  public ContextKey<K, V> asKey() {
    return new ContextKey<>(this.namespace, this.keyClass, this.valueClass);
  }

  public static class MapContextBuilder<K, V> {

    private String namespace;
    private Class<K> keyClass;
    private Class<V> valueClass;
    private RedissonClient redissonClient;
    private Function<K, V> creator;
    private MongoDatabase mongoDatabase;
    private GsonSerializer gsonSerializer;

    public MapContextBuilder<K, V> serializer(GsonSerializer serializer) {
      this.gsonSerializer = serializer;
      return this;
    }

    public MapContextBuilder<K, V> namespace(String namespace) {
      this.namespace = namespace;
      return this;
    }

    public MapContextBuilder<K, V> keyClass(Class<K> keyClass) {
      this.keyClass = keyClass;
      return this;
    }

    public MapContextBuilder<K, V> valueClass(Class<V> valueClass) {
      this.valueClass = valueClass;
      return this;
    }

    public MapContextBuilder<K, V> redissonClient(RedissonClient redissonClient) {
      this.redissonClient = redissonClient;
      return this;
    }

    public MapContextBuilder<K, V> creator(Function<K, V> creator) {
      this.creator = creator;
      return this;
    }

    public MapContextBuilder<K, V> mongoDatabase(MongoDatabase mongoDatabase) {
      this.mongoDatabase = mongoDatabase;
      return this;
    }

    public DataMapContext<K, V> build() {
      return new DataMapContext<>(this.namespace, this.keyClass, this.valueClass, this.redissonClient, this.creator, this.mongoDatabase, this.gsonSerializer);
    }

  }

}
