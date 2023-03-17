package net.collegemc.mc.libs.regions;

import lombok.Getter;
import lombok.Setter;
import net.collegemc.mc.libs.regions.impl.Vec2D;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RegionSelection {

  private Location first;
  private Location second;
  private final List<Location> polySelections = new ArrayList<>();

  public boolean valid() {
    return this.first != null && this.second != null && this.first.getWorld() == this.second.getWorld();
  }

  public void addPolyCorner(Location location) {
    this.polySelections.add(location);
  }

  public void clearPolyCorners() {
    this.polySelections.clear();
  }

  public boolean hasEnoughPolyCorners() {
    return this.polySelections.size() > 2;
  }

  public List<Vec2D> getPolySelectionProjected2D() {
    List<Vec2D> corners = new ArrayList<>();
    for (Location location : this.polySelections) {
      corners.add(new Vec2D(location.getBlockX() + .5, location.getBlockZ() + .5));
    }
    return corners;
  }

}
