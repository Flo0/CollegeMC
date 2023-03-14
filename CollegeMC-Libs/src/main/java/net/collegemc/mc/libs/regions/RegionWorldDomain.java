package net.collegemc.mc.libs.regions;

import net.collegemc.mc.libs.spigot.UtilChunk;
import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionWorldDomain {

  private final Map<Long, RegionChunkDomain> chunkDomainMap = new HashMap<>();

  public Region getRegionWithHighestPriorityAt(Location location) {
    long chunkKey = UtilChunk.getChunkKey(location);
    RegionChunkDomain domain = this.chunkDomainMap.get(chunkKey);
    return domain == null ? null : domain.getRegionWithHighestPriorityAt(location);
  }

  public void addRegion(Region region) {
    for (long chunkId : this.getIntersectingChunks(region)) {
      this.chunkDomainMap.computeIfAbsent(chunkId, key -> new RegionChunkDomain()).addRegion(region);
    }
  }

  public void removeRegion(Region region) {
    for (long chunkId : this.getIntersectingChunks(region)) {
      RegionChunkDomain domain = this.chunkDomainMap.computeIfAbsent(chunkId, key -> new RegionChunkDomain());
      domain.removeRegion(region);
      if (domain.isEmpty()) {
        this.chunkDomainMap.remove(chunkId);
      }
    }
  }

  private List<Long> getIntersectingChunks(Region region) {
    List<Long> chunkIds = new ArrayList<>();
    BoundingBox boundingBox = region.getBoundingBox();
    Vector min = boundingBox.getMin();
    Vector max = boundingBox.getMax();

    int startX = min.getBlockX() >> 4;
    int startZ = min.getBlockZ() >> 4;
    int endX = max.getBlockX() >> 4;
    int endZ = max.getBlockZ() >> 4;

    for (int x = startX; x <= endX; x++) {
      for (int z = startZ; z <= endZ; z++) {
        chunkIds.add(UtilChunk.getChunkKey(x, z));
      }
    }

    return chunkIds;
  }

  public boolean isEmpty() {
    return this.chunkDomainMap.isEmpty();
  }
}
