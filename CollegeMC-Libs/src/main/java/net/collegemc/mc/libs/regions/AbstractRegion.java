package net.collegemc.mc.libs.regions;

import lombok.Getter;
import net.collegemc.mc.libs.regions.permissions.RegionPermissionContainer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public abstract class AbstractRegion implements Comparable<AbstractRegion> {

  @Getter
  private final String name;
  @Getter
  private final int priority;
  @Getter
  private final UUID worldId;
  @Getter
  private final RegionPermissionContainer permissionContainer;

  public AbstractRegion(int priority, String name, UUID worldId) {
    this.priority = priority;
    this.name = name;
    this.worldId = worldId;
    this.permissionContainer = new RegionPermissionContainer();
  }

  @Override
  public int compareTo(@NotNull AbstractRegion other) {
    return Integer.compare(this.priority, other.priority);
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof AbstractRegion other && this.name.equals(other.name);
  }

  public abstract boolean contains(Vector vector);

  public abstract List<Long> getIntersectingChunks();

  public abstract Vector getCenter();
}
