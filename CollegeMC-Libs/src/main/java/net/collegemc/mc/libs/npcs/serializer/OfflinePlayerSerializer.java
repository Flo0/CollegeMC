package net.collegemc.mc.libs.npcs.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R2.CraftOfflinePlayer;

import java.lang.reflect.Type;
import java.util.Map;

public class OfflinePlayerSerializer implements JsonSerializer<OfflinePlayer>, JsonDeserializer<OfflinePlayer> {

  private final TypeToken<Map<String, Object>> token = new TypeToken<>() {
  };

  @Override
  public OfflinePlayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return CraftOfflinePlayer.deserialize(context.deserialize(json, this.token.getType()));
  }

  @Override
  public JsonElement serialize(OfflinePlayer src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src.serialize());
  }
}
