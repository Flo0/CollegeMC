package net.collegemc.mc.core.gson.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.lang.reflect.Type;

public class WorldAdapter implements JsonSerializer<World>, JsonDeserializer<World> {
  @Override
  public World deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return Bukkit.getWorld(json.getAsString());
  }

  @Override
  public JsonElement serialize(World src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.getName());
  }
}
