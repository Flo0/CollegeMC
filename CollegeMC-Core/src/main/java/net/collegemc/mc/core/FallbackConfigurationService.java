package net.collegemc.mc.core;

import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.gson.adapters.ClassAdapter;
import net.collegemc.common.gson.adapters.UUIDAdapter;
import net.collegemc.common.mongodb.MongoDriverProperties;
import net.collegemc.mc.libs.ServerConfigurationService;

import java.util.UUID;

public class FallbackConfigurationService implements ServerConfigurationService {

  @Override
  public GsonSerializer getSerializer() {
    GsonSerializer serializer = new GsonSerializer();
    ClassAdapter classAdapter = new ClassAdapter();
    UUIDAdapter uuidAdapter = new UUIDAdapter();
    serializer.registerTypeAdapter(Class.class, classAdapter);
    serializer.registerTypeAdapter(UUID.class, uuidAdapter);
    return serializer;
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
