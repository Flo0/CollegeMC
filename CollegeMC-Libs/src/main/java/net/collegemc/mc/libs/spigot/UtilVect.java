package net.collegemc.mc.libs.spigot;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UtilVect {

  public static String vecToString(final Vector vector) {
    final ByteBuffer buffer = ByteBuffer.allocate(24);
    buffer.rewind();
    buffer.putDouble(vector.getX());
    buffer.putDouble(vector.getY());
    buffer.putDouble(vector.getZ());
    return new String(buffer.array());
  }

  public static void forEachPoint(Location base, final Vector dir, final double delta, final Consumer<Location> consumer) {
    base = base.clone();
    final int iterations = (int) (dir.length() / delta);
    final Vector deltaVec = dir.clone().normalize().multiply(delta);
    for (int i = 0; i < iterations; i++) {
      consumer.accept(base);
      base = base.add(deltaVec);
    }
  }

  public static void applyToEdges(World world, BoundingBox box, double delta, Consumer<Location> consumer) {
    final Location iii = new Location(world, box.getMinX(), box.getMinY(), box.getMinZ());
    final Location iai = new Location(world, box.getMinX(), box.getMaxY(), box.getMinZ());
    final Location aii = new Location(world, box.getMaxX(), box.getMinY(), box.getMinZ());
    final Location aia = new Location(world, box.getMaxX(), box.getMinY(), box.getMaxZ());
    final Location iia = new Location(world, box.getMinX(), box.getMinY(), box.getMaxZ());
    final Location aai = new Location(world, box.getMaxX(), box.getMaxY(), box.getMinZ());
    final Location iaa = new Location(world, box.getMinX(), box.getMaxY(), box.getMaxZ());
    final Location aaa = new Location(world, box.getMaxX(), box.getMaxY(), box.getMaxZ());

    final List<Location> points = new ArrayList<>();

    points.addAll(getPointsBetween(iii, aii, delta));
    points.addAll(getPointsBetween(iii, iia, delta));
    points.addAll(getPointsBetween(iii, iai, delta));

    points.addAll(getPointsBetween(aia, aii, delta));
    points.addAll(getPointsBetween(aia, iia, delta));

    points.addAll(getPointsBetween(iai, aai, delta));
    points.addAll(getPointsBetween(iai, iaa, delta));
    points.addAll(getPointsBetween(iaa, iia, delta));

    points.addAll(getPointsBetween(aii, aai, delta));
    points.addAll(getPointsBetween(aii, aai, delta));

    points.addAll(getPointsBetween(aaa, aai, delta));
    points.addAll(getPointsBetween(aaa, iaa, delta));
    points.addAll(getPointsBetween(aaa, aia, delta));

    points.forEach(consumer);
  }

  public static void forEachPositionBetween(Location locA, Location locB, double delta, Consumer<Location> consumer) {
    getPointsBetween(locA, locB, delta).forEach(consumer);
  }

  public static void forEachBlock(final BoundingBox box, final World world, final Consumer<Block> blockConsumer) {
    for (int x = (int) box.getMinX(); x <= (int) box.getMaxX() - 1; x++) {
      for (int y = (int) box.getMinY(); y <= (int) box.getMaxY() - 1; y++) {
        for (int z = (int) box.getMinZ(); z <= (int) box.getMaxZ() - 1; z++) {
          blockConsumer.accept(world.getBlockAt(x, y, z));
        }
      }
    }
  }

  public static void showBoundingBox(final BoundingBox box, final Player player, final int viewDist) {
    final World world = player.getWorld();
    final double maxDistSq = viewDist * viewDist;
    final Location iii = new Location(world, box.getMinX(), box.getMinY(), box.getMinZ());
    final Location iai = new Location(world, box.getMinX(), box.getMaxY(), box.getMinZ());
    final Location aii = new Location(world, box.getMaxX(), box.getMinY(), box.getMinZ());
    final Location aia = new Location(world, box.getMaxX(), box.getMinY(), box.getMaxZ());
    final Location iia = new Location(world, box.getMinX(), box.getMinY(), box.getMaxZ());
    final Location aai = new Location(world, box.getMaxX(), box.getMaxY(), box.getMinZ());
    final Location iaa = new Location(world, box.getMinX(), box.getMaxY(), box.getMaxZ());
    final Location aaa = new Location(world, box.getMaxX(), box.getMaxY(), box.getMaxZ());

    final List<Location> points = new ArrayList<>();

    points.addAll(getPointsBetween(iii, aii));
    points.addAll(getPointsBetween(iii, iia));
    points.addAll(getPointsBetween(iii, iai));

    points.addAll(getPointsBetween(aia, aii));
    points.addAll(getPointsBetween(aia, iia));

    points.addAll(getPointsBetween(iai, aai));
    points.addAll(getPointsBetween(iai, iaa));
    points.addAll(getPointsBetween(iaa, iia));

    points.addAll(getPointsBetween(aii, aai));
    points.addAll(getPointsBetween(aii, aai));

    points.addAll(getPointsBetween(aaa, aai));
    points.addAll(getPointsBetween(aaa, iaa));
    points.addAll(getPointsBetween(aaa, aia));

    particles(points, player.getLocation(), maxDistSq);
  }

  public static List<Location> getPointsBetween(Location from, Location to) {
    return getPointsBetween(from, to, 0.75);
  }

  public static List<Location> getPointsBetween(Location from, Location to, double dist) {
    final List<Location> locations = new ArrayList<>();
    from = from.clone();
    to = to.clone();
    final Vector dir = to.toVector().subtract(from.toVector());
    final int increments = (int) (dir.length() / dist);
    dir.normalize().multiply(dist);
    for (int i = 0; i < increments; i++) {
      from.add(dir);
      locations.add(from.clone());
    }
    return locations;
  }

  private static void particles(final List<Location> locations, final Location view, final double distSq) {
    final DustOptions options = new DustOptions(Color.RED, 1F);
    final World world = view.getWorld();
    for (final Location point : locations) {
      if (view.toVector().subtract(point.toVector()).lengthSquared() < distSq) {
        world.spawnParticle(Particle.REDSTONE, point, 1, 0, 0, 0, 0, options);
      }
    }
  }

}
