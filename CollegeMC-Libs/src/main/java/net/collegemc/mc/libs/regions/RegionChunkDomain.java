package net.collegemc.mc.libs.regions;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class RegionChunkDomain {

  private final List<AbstractRegion> regions = new ArrayList<>();

  public AbstractRegion getRegionWithHighestPriorityAt(Location location) {
    Vector vector = location.toVector();
    return this.regions.stream()
            .filter(region -> region.contains(vector))
            .max(AbstractRegion::compareTo)
            .orElse(null);
  }

  public void addRegion(AbstractRegion region) {
    this.regions.add(region);
  }

  public void removeRegion(AbstractRegion region) {
    this.regions.remove(region);
  }

  public boolean isEmpty() {
    return this.regions.isEmpty();
  }

}
