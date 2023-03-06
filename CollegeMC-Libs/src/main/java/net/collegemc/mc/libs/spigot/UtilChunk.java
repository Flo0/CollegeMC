package net.collegemc.mc.libs.spigot;


import com.google.common.base.Preconditions;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*******************************************************
 * Copyright (C) Gestankbratwurst suotokka@gmail.com
 *
 * This file is part of LaLaLand-CorePlugin and was created at the 17.11.2019
 *
 * LaLaLand-CorePlugin can not be copied and/or distributed without the express
 * permission of the owner.
 *
 */
public class UtilChunk {

  public static int[] getChunkCoords(final long chunkKey) {
    final int x = ((int) chunkKey);
    final int z = (int) (chunkKey >> 32);
    return new int[]{x, z};
  }

  public static long getChunkKey(final int x, final int z) {
    return (long) x & 0xFFFFFFFFL | ((long) z & 0xFFFFFFFFL) << 32;
  }

  public static long getChunkKey(final Chunk chunk) {
    return getChunkKey(chunk.getX(), chunk.getZ());
  }

  public static Chunk keyToChunk(final World world, final long chunkID) {
    Preconditions.checkArgument(world != null, "World cannot be null");
    return world.getChunkAt((int) chunkID, (int) (chunkID >> 32));
  }

  public static boolean isChunkLoaded(final Location loc) {
    final int chunkX = loc.getBlockX() >> 4;
    final int chunkZ = loc.getBlockZ() >> 4;
    final World world = loc.getWorld();
    if (world == null) {
      return false;
    }
    return world.isChunkLoaded(chunkX, chunkZ);
  }

  public static long getChunkKey(final Location loc) {
    return getChunkKey(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
  }

  public static long getChunkKey(final ChunkSnapshot chunk) {
    return (long) chunk.getX() & 0xffffffffL | ((long) chunk.getZ() & 0xffffffffL) << 32;
  }

  public static LongSet getChunkViews(Player player) {
    return ChunkTrackListener.chunkViews.get(player.getUniqueId());
  }

  public static boolean isChunkInView(Player player, Chunk chunk) {
    return ChunkTrackListener.chunkViews.get(player.getUniqueId()).contains(chunk.getChunkKey());
  }

  public static int relativeKeyOf(Block block) {
    final int relX = (block.getX() % 16 + 16) % 16;
    final int relZ = (block.getZ() % 16 + 16) % 16;
    final int relY = block.getY();
    return (relY & 0xFFFF) | ((relX & 0xFF) << 16) | ((relZ & 0xFF) << 24);
  }

  public static int blockKeyToX(int key) {
    return (key >> 16) & 0xFF;
  }

  public static int blockKeyToY(int key) {
    return key & 0xFFFF;
  }

  public static int blockKeyToZ(int key) {
    return (key >> 24) & 0xFF;
  }

  public static class ChunkTrackListener implements Listener {

    private static final Map<UUID, LongSet> chunkViews = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
      chunkViews.put(event.getPlayer().getUniqueId(), new LongOpenHashSet());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
      chunkViews.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onReceive(PlayerChunkLoadEvent event) {
      chunkViews.get(event.getPlayer().getUniqueId()).add(event.getChunk().getChunkKey());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerChunkUnloadEvent event) {
      chunkViews.get(event.getPlayer().getUniqueId()).remove(event.getChunk().getChunkKey());
    }

  }

}
