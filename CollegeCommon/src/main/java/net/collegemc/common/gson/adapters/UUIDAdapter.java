package net.collegemc.common.gson.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {
  @Override
  public UUID deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
    return UUID.fromString(jsonElement.getAsJsonPrimitive().getAsString());
  }

  @Override
  public JsonElement serialize(UUID uuid, Type type, JsonSerializationContext jsonSerializationContext) {
    return new JsonPrimitive(uuid.toString());
  }
}
