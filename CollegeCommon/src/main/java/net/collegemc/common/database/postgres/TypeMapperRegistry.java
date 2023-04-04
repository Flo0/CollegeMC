package net.collegemc.common.database.postgres;

import java.util.HashMap;
import java.util.Map;

public class TypeMapperRegistry {

  private final Map<Class<?>, String> typeMap = new HashMap<>();
  private final Map<Class<?>, SQLAccess.IndexedPreparation<Object>> preparationConsumer = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <T> void registerPreparation(Class<T> clazz, SQLAccess.IndexedPreparation<T> consumer) {
    preparationConsumer.put(clazz, (SQLAccess.IndexedPreparation<Object>) consumer);
  }

  public <T> SQLAccess.IndexedPreparation<Object> getPreparation(Class<T> clazz) {
    return preparationConsumer.get(clazz);
  }

  public void registerType(Class<?> clazz, String type) {
    typeMap.put(clazz, type);
  }

  public String getDBType(Class<?> clazz) {
    return typeMap.get(clazz);
  }

}
