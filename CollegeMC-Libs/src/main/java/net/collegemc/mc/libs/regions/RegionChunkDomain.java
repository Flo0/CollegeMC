package net.collegemc.mc.libs.regions;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class RegionChunkDomain {

  private final List<Region> regions = new ArrayList<>();

  public Region getRegionWithHighestPriorityAt(Location location) {
    Vector vector = location.toVector();
    return this.regions.stream()
            .filter(region -> region.getBoundingBox().contains(vector))
            .max(Region::compareTo)
            .orElse(null);
  }

  public void addRegion(Region region) {
    this.regions.add(region);
  }

  public void removeRegion(Region region) {
    this.regions.remove(region);
  }

  public boolean isEmpty() {
    return this.regions.isEmpty();
  }

}
