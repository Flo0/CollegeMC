package net.collegemc.common.database.redis;

import net.collegemc.common.gson.GsonSerializer;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

public class RedisGsonCodec extends BaseCodec {

  private final GsonEncoder encoder;
  private final GsonDecoder decoder;
  private final Class<? extends GsonSerializer> gsonSerializerClass;

  public RedisGsonCodec(GsonSerializer gsonSerializer) {
    this.encoder = new GsonEncoder(gsonSerializer);
    this.decoder = new GsonDecoder(gsonSerializer);
    this.gsonSerializerClass = gsonSerializer.getClass();
  }

  @Override
  public Decoder<Object> getValueDecoder() {
    return this.decoder;
  }

  @Override
  public Encoder getValueEncoder() {
    return this.encoder;
  }

  @Override
  public ClassLoader getClassLoader() {
    if (gsonSerializerClass.getClassLoader() != null) {
      return gsonSerializerClass.getClassLoader();
    }
    return super.getClassLoader();
  }
}
