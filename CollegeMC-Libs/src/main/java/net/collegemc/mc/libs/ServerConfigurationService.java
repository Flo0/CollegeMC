package net.collegemc.mc.libs;

import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.mongodb.MongoDriverProperties;

import java.io.File;

public interface ServerConfigurationService {

  GsonSerializer getSerializer();

  String getRedisAddress();

  MongoDriverProperties getMongoDriverProperties();

  String getServerName();

  default String resourcepackServerHost() {
    return "127.0.0.1";
  }

  default int resourcepackServerPort() {
    return 7786;
  }

  default boolean isResourcepackEnabled() {
    return false;
  }

  default File getRawResourcepackFiles() {
    return null;
  }

  default String getMessagePrefix() {
    return "CollegeMC";
  }

  default int getRedisRetryInterval() {
    return 2000;
  }

  default int getRedisTimeout() {
    return 5000;
  }

  default int getRedisConnectionPoolSize() {
    return 64;
  }

  default int getRedisNettyThreads() {
    return 32;
  }

}