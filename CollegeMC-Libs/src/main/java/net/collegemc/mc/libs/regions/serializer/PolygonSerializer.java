package net.collegemc.mc.libs.regions.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.awt.Polygon;
import java.lang.reflect.Type;

public class PolygonSerializer implements JsonSerializer<Polygon>, JsonDeserializer<Polygon> {
  @Override
  public Polygon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject jsonObject = json.getAsJsonObject();

    int nPoints = jsonObject.get("nPoints").getAsInt();
    int[] xPoints = context.deserialize(jsonObject.get("xPoints"), int[].class);
    int[] yPoints = context.deserialize(jsonObject.get("yPoints"), int[].class);

    return new Polygon(xPoints, yPoints, nPoints);
  }

  @Override
  public JsonElement serialize(Polygon src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObject = new JsonObject();

    jsonObject.addProperty("nPoints", src.npoints);
    jsonObject.add("xPoints", context.serialize(src.xpoints));
    jsonObject.add("yPoints", context.serialize(src.ypoints));

    return jsonObject;
  }
}
