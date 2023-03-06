package net.collegemc.mc.libs.resourcepack.assembly;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class PackMeta {

  protected PackMeta(final int packFormat, final String description) {
    this.packFormat = packFormat;
    this.description = description;
    this.gson = new GsonBuilder().setPrettyPrinting().create();
  }

  private final Gson gson;
  private final int packFormat;
  private final String description;

  public String getAsJsonString() {
    final JsonObject json = new JsonObject();
    final JsonObject packJson = new JsonObject();
    packJson.addProperty("pack_format", this.packFormat);
    packJson.addProperty("description", this.description);
    json.add("pack", packJson);
    return this.gson.toJson(json);
  }

}
