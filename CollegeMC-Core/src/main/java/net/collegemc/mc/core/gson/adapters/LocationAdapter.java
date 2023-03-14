package net.collegemc.mc.core.gson.adapters;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.Map;

public class LocationAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

  private final TypeToken<Map<String, Object>> token = new TypeToken<>() {
  };

  @Override
  public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return Location.deserialize(context.deserialize(json, this.token.getType()));
  }

  @Override
  public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src.serialize());
  }

}
