package net.collegemc.common.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import net.collegemc.common.gson.adapters.ClassAdapter;
import net.collegemc.common.gson.adapters.UUIDAdapter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class GsonSerializer {

  private final Gson prettyProxy = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
  private final Set<Type> typeSet = new HashSet<>();
  private Gson currentGson;
  private final Map<Object, Consumer<GsonBuilder>> builderConsumer = new HashMap<>();
  private final Map<Object, Gson> adapterSkipMap = new HashMap<>();
  private boolean changed = true;

  public GsonSerializer() {
    registerTypeAdapter(UUID.class, new UUIDAdapter());
    registerTypeAdapter(Class.class, new ClassAdapter());
  }

  public Gson getGson() {
    checkForChanges();
    return currentGson;
  }

  public Gson getGsonWithout(Object adapter) {
    checkForChanges();
    return adapterSkipMap.get(adapter);
  }

  private void checkForChanges() {
    if (changed) {
      changed = false;
      GsonBuilder builder = new GsonBuilder().disableHtmlEscaping().enableComplexMapKeySerialization();
      builderConsumer.values().forEach(consumer -> consumer.accept(builder));
      currentGson = builder.create();

      for (Object adapter : List.copyOf(builderConsumer.keySet())) {
        GsonBuilder skipBuilder = new GsonBuilder();
        builderConsumer.forEach((cAdapter, consumer) -> {
          if (cAdapter != adapter) {
            consumer.accept(skipBuilder);
          }
        });
        adapterSkipMap.put(adapter, skipBuilder.create());
      }
    }
  }

  public void register(Object adapter, Consumer<GsonBuilder> consumer) {
    builderConsumer.put(adapter, consumer);
    changed = true;
  }

  public <T> String toJson(T object) {
    checkForChanges();
    return currentGson.toJson(object);
  }

  public <T> String pretty(T object) {
    return prettyProxy.toJson(toJsonTree(object));
  }

  public <T> JsonElement toJsonTree(T object) {
    checkForChanges();
    return currentGson.toJsonTree(object);
  }

  public <T> T fromJson(String json, Type type) {
    checkForChanges();
    return currentGson.fromJson(json, type);
  }

  public <T> T fromJsonTree(JsonElement json, Type type) {
    checkForChanges();
    return currentGson.fromJson(json, type);
  }

  public <T> void registerAbstractTypeHierarchyAdapter(Class<T> tClass) {
    registerTypeHierarchyAdapter(tClass, new AbstractClassAdapter(this));
  }

  public void registerAbstractTypeAdapter(Type type) {
    registerTypeAdapter(type, new AbstractClassAdapter(this));
  }

  public void registerTypeAdapterFactory(TypeAdapterFactory typeAdapterFactory) {
    register(typeAdapterFactory, builder -> builder.registerTypeAdapterFactory(typeAdapterFactory));
  }

  public void registerTypeAdapter(Type type, Object adapter) {
    typeSet.add(type);
    register(adapter, builder -> builder.registerTypeAdapter(type, adapter));
  }

  public <T> void registerTypeHierarchyAdapter(Class<T> tClass, Object adapter) {
    register(adapter, builder -> builder.registerTypeHierarchyAdapter(tClass, adapter));
  }

  public <T> TypeAdapter<T> getAdapter(TypeToken<T> tTypeToken) {
    return getGson().getAdapter(tTypeToken);
  }

  public boolean hasTypeAdapter(Type type) {
    return typeSet.contains(type);
  }
}
