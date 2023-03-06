package net.collegemc.mc.libs.resourcepack.assembly;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public record ModelData(String modelParent, JsonObject displayJson, JsonObject elementsJson) {

  public static ModelData of(final String modelParent, final JsonObject displayJson, final JsonObject elementsJson) {
    return new ModelData(modelParent, displayJson, elementsJson);
  }

  public static ModelData defaultGenerated() {
    return ModelData.of("item/generated", null, null);
  }

  public static ModelData defaultHandheld() {
    return ModelData.of("item/handheld", null, null);
  }

  public static ModelData handheldScaled(final double scale, final double dx, final double dy, final double dz) {
    final String parent = "item/handheld";
    final JsonObject displayJson = new JsonObject();
    final JsonObject handScaleJson = new JsonObject();
    final JsonArray translationArray = new JsonArray();
    final JsonArray scaleArray = new JsonArray();
    translationArray.add(dx);
    translationArray.add(dy);
    translationArray.add(dz);
    scaleArray.add(scale);
    scaleArray.add(scale);
    scaleArray.add(scale);
    final JsonArray guiArray = new JsonArray();
    final JsonArray guiArrayTrans = new JsonArray();
    guiArray.add(scale);
    guiArray.add(scale);
    guiArray.add(scale);
    guiArrayTrans.add(scale);
    guiArrayTrans.add(scale);
    guiArrayTrans.add(scale);
    final JsonObject guiJson = new JsonObject();
    guiJson.add("scale", guiArray);
    guiJson.add("translation", guiArrayTrans);
    handScaleJson.add("translation", translationArray);
    handScaleJson.add("scale", scaleArray);
    displayJson.add("thirdperson_righthand", handScaleJson);
    displayJson.add("thirdperson_lefthand", handScaleJson);
    displayJson.add("firstperson_lefthand", handScaleJson);
    displayJson.add("firstperson_righthand", handScaleJson);
    displayJson.add("gui", guiJson);
    return ModelData.of(parent, displayJson, null);
  }

}