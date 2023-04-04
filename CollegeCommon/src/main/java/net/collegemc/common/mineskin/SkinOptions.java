package net.collegemc.common.mineskin;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import org.jsoup.Connection;

public class SkinOptions {
  private static final String URL_FORMAT = "name=%s&model=%s&visibility=%s";
  private final String name;
  private final Variant variant;
  private final Visibility visibility;

  private SkinOptions(String name, Variant variant, Visibility visibility) {
    this.name = name;
    this.variant = variant;
    this.visibility = visibility;
  }

  protected JsonObject toJson() {
    JsonObject json = new JsonObject();
    if (!Strings.isNullOrEmpty(this.name)) {
      json.addProperty("name", this.name);
    }

    if (this.variant != null && this.variant != Variant.AUTO) {
      json.addProperty("variant", this.variant.getName());
    }

    if (this.visibility != null) {
      json.addProperty("visibility", this.visibility.getCode());
    }

    return json;
  }

  protected void addAsData(Connection connection) {
    if (!Strings.isNullOrEmpty(this.name)) {
      connection.data("name", this.name);
    }

    if (this.variant != null && this.variant != Variant.AUTO) {
      connection.data("variant", this.variant.getName());
    }

    if (this.visibility != null) {
      connection.data("visibility", String.valueOf(this.visibility.getCode()));
    }

  }

  public String getName() {
    return this.name;
  }

  public static SkinOptions create(String name, Variant variant, Visibility visibility) {
    return new SkinOptions(name, variant, visibility);
  }

  public static SkinOptions name(String name) {
    return new SkinOptions(name, Variant.AUTO, Visibility.PUBLIC);
  }

  public static SkinOptions none() {
    return new SkinOptions("", Variant.AUTO, Visibility.PUBLIC);
  }
}
