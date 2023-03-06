package net.collegemc.velocity;

import lombok.Data;
import lombok.Getter;
import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.mongodb.MongoDriverProperties;

@Data
public class ProxyConfig {

  private transient GsonSerializer gsonSerializer = new GsonSerializer();
  private String redisAddress = "127.0.0.1";
  private int redisPort = 6379;
  private String mongoAddress = "127.0.0.1";
  private int mongoPort = 27017;
  private String mongoUser = "admin";
  private String mongoPassword = "admin";
  @Getter
  private int redisNettyThreads = 16;
  @Getter
  private int redisRetryInterval = 2000;
  @Getter
  private int redisTimeout = 5000;
  @Getter
  private int redisConnectionPoolSize = 32;

  public GsonSerializer getSerializer() {
    return gsonSerializer;
  }

  public String getRedisAddress() {
    return "redis://%s:%d".formatted(redisAddress, redisPort);
  }

  public MongoDriverProperties getMongoDriverProperties() {
    return MongoDriverProperties.builder()
            .hostAddress(mongoAddress)
            .hostPort(mongoPort)
            .user(mongoUser)
            .password(mongoPassword)
            .build();
  }

}
