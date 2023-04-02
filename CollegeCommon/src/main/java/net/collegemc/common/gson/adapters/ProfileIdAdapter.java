package net.collegemc.common.gson.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.collegemc.common.network.data.college.ProfileId;

import java.lang.reflect.Type;
import java.util.UUID;

public class ProfileIdAdapter implements JsonSerializer<ProfileId>, JsonDeserializer<ProfileId> {
  @Override
  public ProfileId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return new ProfileId(context.deserialize(json, UUID.class));
  }

  @Override
  public JsonElement serialize(ProfileId src, Type typeOfSrc, JsonSerializationContext context) {
    return context.serialize(src.getUid());
  }
}
