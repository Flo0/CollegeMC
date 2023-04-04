package net.collegemc.mc.core;

import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.database.mongodb.MongoDriverProperties;
import net.collegemc.mc.core.gson.CollegeGsonSerializer;
import net.collegemc.mc.libs.ServerConfigurationService;

public class FallbackConfigurationService implements ServerConfigurationService {

  @Override
  public GsonSerializer getSerializer() {
    return new CollegeGsonSerializer();
  }

  @Override
  public String getRedisAddress() {
    return "redis://127.0.0.1:6379";
  }

  @Override
  public MongoDriverProperties getMongoDriverProperties() {
    return MongoDriverProperties.builder()
            .hostAddress("127.0.0.1")
            .hostPort(27017)
            .user("admin")
            .password("admin")
            .build();
  }

  @Override
  public String getServerName() {
    return "Debug-Server";
  }
}
