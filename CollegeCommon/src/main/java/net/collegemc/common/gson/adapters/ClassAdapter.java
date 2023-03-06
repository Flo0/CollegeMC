package net.collegemc.common.gson.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class ClassAdapter implements JsonSerializer<Class>, JsonDeserializer<Class> {
  @Override
  public Class deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    try {
      return Class.forName(jsonElement.getAsJsonPrimitive().getAsString());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public JsonElement serialize(Class aClass, Type type, JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(aClass.getName());
  }
}
