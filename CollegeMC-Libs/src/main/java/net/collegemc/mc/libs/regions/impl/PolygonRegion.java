package net.collegemc.mc.libs.regions.impl;

import com.google.common.base.Preconditions;
import net.collegemc.common.utils.Pair;
import net.collegemc.mc.libs.regions.AbstractRegion;
import net.collegemc.mc.libs.spigot.UtilChunk;
import net.collegemc.mc.libs.spigot.UtilVect;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PolygonRegion extends AbstractRegion {

  private final Polygon polygon;
  private final List<Vec2D> corners;
  private final double base;
  private final double height;

  public PolygonRegion(int priority, String name, UUID worldId, List<Vec2D> corners, double base, double height) {
    super(priority, name, worldId);
    Preconditions.checkArgument(corners.size() > 2);
    int[] xs = new int[corners.size()];
    int[] ys = new int[corners.size()];

    for (int i = 0; i < corners.size(); i++) {
      xs[i] = (int) corners.get(i).getX();
      ys[i] = (int) corners.get(i).getZ();
    }

    this.polygon = new Polygon(xs, ys, corners.size());
    this.corners = List.copyOf(corners);
    this.height = height;
    this.base = base;

    TaskManager.runTaskTimer(this::display, 5, 5);
  }

  @Override
  public boolean contains(Vector vector) {
    double y = vector.getY();
    if (y < this.base || y > this.base + this.height) {
      return false;
    }
    double scaledX = vector.getX();
    double scaledZ = vector.getZ();
    return this.polygon.contains(scaledX, scaledZ);
  }

  @Override
  public List<Long> getIntersectingChunks() {
    List<Long> chunks = new ArrayList<>();
    Rectangle2D bounds = this.polygon.getBounds();

    int startX = NumberConversions.floor(bounds.getMinX()) >> 4;
    int startZ = NumberConversions.floor(bounds.getMinY()) >> 4;
    int endX = NumberConversions.floor(bounds.getMaxX()) >> 4;
    int endZ = NumberConversions.floor(bounds.getMaxY()) >> 4;

    for (int x = startX; x <= endX; x++) {
      for (int z = startZ; z <= endZ; z++) {
        Rectangle2D rectangle = new Rectangle(x * 16, z * 16, 16, 16);
        System.out.println("Checking [" + x + "|" + z + "] (x:" + (int) rectangle.getMinX() + "-" + (int) rectangle.getMaxX() + "|z:" + rectangle.getMinY() + "-" + rectangle.getMaxY() + ")");
        if (this.polygon.intersects(rectangle)) {
          chunks.add(UtilChunk.getChunkKey(x, z));
        }
      }
    }

    System.out.println("Intersecting with " + chunks.size() + " chunks.");

    return chunks;
  }

  @Override
  public Vector getCenter() {
    Rectangle2D bounds = this.polygon.getBounds2D();
    return new Vector(bounds.getCenterX(), this.base + this.height / 2.0, bounds.getCenterY());
  }

  public List<Pair<Location, Location>> getSpanCorners() {
    List<Pair<Location, Location>> vectors = new ArrayList<>();
    World world = Bukkit.getWorld(this.getWorldId());
    Preconditions.checkState(world != null);

    for (int i = 0; i < this.corners.size(); i++) {
      Vec2D from2D = this.corners.get(i);
      Vec2D to2D = i == this.corners.size() - 1 ? this.corners.get(0) : this.corners.get(i + 1);
      Vector upperFrom = new Vector(from2D.getX(), this.base + this.height, from2D.getZ());
      Vector upperTo = new Vector(to2D.getX(), this.base + this.height, to2D.getZ());
      Vector lowerFrom = new Vector(from2D.getX(), this.base, from2D.getZ());
      Vector lowerTo = new Vector(to2D.getX(), this.base, to2D.getZ());

      Location upperFromLoc = upperFrom.toLocation(world);
      Location upperToLoc = upperTo.toLocation(world);
      Location lowerFromLoc = lowerFrom.toLocation(world);
      Location lowerToLoc = lowerTo.toLocation(world);

      vectors.add(new Pair<>(upperFromLoc, upperToLoc));
      vectors.add(new Pair<>(lowerFromLoc, lowerToLoc));
      vectors.add(new Pair<>(upperFromLoc, lowerFromLoc));
    }

    return vectors;
  }

  private void display() {
    World world = Bukkit.getWorld(this.getWorldId());
    double distance = 0.1;
    for (Pair<Location, Location> locationPair : this.getSpanCorners()) {
      UtilVect.forEachPositionBetween(locationPair.key(), locationPair.value(), distance, loc -> {
        world.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 0.33f));
      });
    }
  }
}
