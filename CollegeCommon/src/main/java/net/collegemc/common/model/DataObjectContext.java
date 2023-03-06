package net.collegemc.common.model;

import com.mongodb.client.MongoDatabase;
import net.collegemc.common.gson.GsonSerializer;
import org.redisson.api.RedissonClient;

public class DataObjectContext<T> extends DataMapContext<String, T> {

  public static <T> ObjectContextBuilder<T> objectContextBuilder() {
    return new ObjectContextBuilder<>();
  }

  public DataObjectContext(String namespace,
                           Class<T> valueClass,
                           RedissonClient redissonClient,
                           T defaultObject,
                           MongoDatabase mongoDatabase,
                           GsonSerializer gsonSerializer
  ) {
    super(namespace, String.class, valueClass, redissonClient, (k) -> defaultObject, mongoDatabase, gsonSerializer);
  }

  public static class ObjectContextBuilder<T> {

    private String namespace;
    private Class<T> valueClass;
    private RedissonClient redissonClient;
    private T defaultObject;
    private MongoDatabase mongoDatabase;
    private GsonSerializer gsonSerializer;

    public ObjectContextBuilder<T> serializer(GsonSerializer serializer) {
      this.gsonSerializer = serializer;
      return this;
    }

    public ObjectContextBuilder<T> namespace(String namespace) {
      this.namespace = namespace;
      return this;
    }

    public ObjectContextBuilder<T> valueClass(Class<T> valueClass) {
      this.valueClass = valueClass;
      return this;
    }

    public ObjectContextBuilder<T> redissonClient(RedissonClient redissonClient) {
      this.redissonClient = redissonClient;
      return this;
    }

    public ObjectContextBuilder<T> defaultObj(T defaultObj) {
      this.defaultObject = defaultObj;
      return this;
    }

    public ObjectContextBuilder<T> mongoDatabase(MongoDatabase mongoDatabase) {
      this.mongoDatabase = mongoDatabase;
      return this;
    }

    public DataObjectContext<T> build() {
      return new DataObjectContext<>(this.namespace, this.valueClass, this.redissonClient, this.defaultObject, this.mongoDatabase, gsonSerializer);
    }

  }

}
