package net.collegemc.mc.libs.regions;

import lombok.Getter;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Region implements Comparable<Region> {

  @Getter
  private final String name;
  @Getter
  private final int priority;
  @Getter
  private final BoundingBox boundingBox;
  @Getter
  private final UUID worldId;

  public Region(int priority, String name, BoundingBox boundingBox, UUID worldId) {
    this.priority = priority;
    this.name = name;
    this.boundingBox = boundingBox;
    this.worldId = worldId;
  }

  public Region(String name, BoundingBox boundingBox, UUID worldId) {
    this(0, name, boundingBox, worldId);
  }

  @Override
  public int compareTo(@NotNull Region other) {
    return Integer.compare(this.priority, other.priority);
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Region other && this.name.equals(other.name);
  }
}
