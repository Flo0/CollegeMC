package net.collegemc.mc.libs.regions.impl;

import net.collegemc.mc.libs.regions.AbstractRegion;
import net.collegemc.mc.libs.spigot.UtilChunk;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BoxRegion extends AbstractRegion {

  private final BoundingBox boundingBox;

  public BoxRegion(int priority, String name, BoundingBox boundingBox, UUID worldId) {
    super(priority, name, worldId);
    this.boundingBox = boundingBox;
  }

  @Override
  public boolean contains(Vector vector) {
    return this.boundingBox.contains(vector);
  }

  @Override
  public List<Long> getIntersectingChunks() {
    List<Long> chunkIds = new ArrayList<>();
    Vector min = this.boundingBox.getMin();
    Vector max = this.boundingBox.getMax();

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

  @Override
  public Vector getCenter() {
    return this.boundingBox.getCenter();
  }
}
