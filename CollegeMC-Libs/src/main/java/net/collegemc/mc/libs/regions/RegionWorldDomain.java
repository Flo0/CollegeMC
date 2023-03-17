package net.collegemc.mc.libs.regions;

import net.collegemc.mc.libs.spigot.UtilChunk;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class RegionWorldDomain {

  private final Map<Long, RegionChunkDomain> chunkDomainMap = new HashMap<>();

  public AbstractRegion getRegionWithHighestPriorityAt(Location location) {
    long chunkKey = UtilChunk.getChunkKey(location);
    RegionChunkDomain domain = this.chunkDomainMap.get(chunkKey);
    return domain == null ? null : domain.getRegionWithHighestPriorityAt(location);
  }

  public void addRegion(AbstractRegion region) {
    for (long chunkId : region.getIntersectingChunks()) {
      this.chunkDomainMap.computeIfAbsent(chunkId, key -> new RegionChunkDomain()).addRegion(region);
    }
  }

  public void removeRegion(AbstractRegion region) {
    for (long chunkId : region.getIntersectingChunks()) {
      RegionChunkDomain domain = this.chunkDomainMap.computeIfAbsent(chunkId, key -> new RegionChunkDomain());
      domain.removeRegion(region);
      if (domain.isEmpty()) {
        this.chunkDomainMap.remove(chunkId);
      }
    }
  }


  public boolean isEmpty() {
    return this.chunkDomainMap.isEmpty();
  }
}
