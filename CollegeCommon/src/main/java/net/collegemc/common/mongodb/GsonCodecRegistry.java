package net.collegemc.common.mongodb;

import net.collegemc.common.gson.GsonSerializer;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.HashMap;
import java.util.Map;

public class GsonCodecRegistry implements CodecRegistry {

  private final GsonSerializer gsonSerializer;
  private final Map<Class<?>, Codec<?>> codecCache = new HashMap<>();

  public GsonCodecRegistry(GsonSerializer gsonSerializer) {
    this.gsonSerializer = gsonSerializer;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Codec<T> get(Class<T> clazz) {
    return (Codec<T>) this.codecCache.computeIfAbsent(clazz, (key) -> new MongoGsonCodec<>(key, this.gsonSerializer));
  }

  @Override
  public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
    return this.get(clazz);
  }
}
