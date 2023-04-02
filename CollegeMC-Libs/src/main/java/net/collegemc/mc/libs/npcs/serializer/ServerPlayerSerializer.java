package net.collegemc.mc.libs.npcs.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;

import java.lang.reflect.Type;
import java.util.UUID;

public class ServerPlayerSerializer implements JsonSerializer<ServerPlayer>, JsonDeserializer<ServerPlayer> {
  @Override
  public ServerPlayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject jsonObject = json.getAsJsonObject();

    GameProfile profile = context.deserialize(jsonObject.get("profile"), GameProfile.class);
    UUID worldId = context.deserialize(jsonObject.get("world"), UUID.class);
    ServerLevel world = ((CraftWorld) Bukkit.getWorld(worldId)).getHandle();
    
    return new ServerPlayer(MinecraftServer.getServer(), world, profile);
  }

  @Override
  public JsonElement serialize(ServerPlayer src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObject = new JsonObject();

    GameProfile profile = src.getGameProfile();
    jsonObject.add("profile", context.serialize(profile));
    jsonObject.add("world", context.serialize(src.getLevel().getWorld().getUID()));

    return jsonObject;
  }
}
