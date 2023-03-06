package net.collegemc.mc.libs.holograms;


import net.collegemc.mc.libs.holograms.abstraction.Hologram;
import net.collegemc.mc.libs.holograms.abstraction.HologramFactory;
import net.collegemc.mc.libs.spigot.UtilChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class HologramManager {

  private final Map<String, Hologram> hologramMap = new HashMap<>();
  private final Map<UUID, Map<Long, List<Hologram>>> hologramWorldMap = new HashMap<>();
  private final HologramFactory hologramFactory;

  public HologramManager(HologramFactory hologramFactory) {
    this.hologramFactory = hologramFactory;
  }

  public Hologram getHologram(String hologramID) {
    return this.hologramMap.get(hologramID);
  }

  public Hologram createHologram(Location location, String name) {
    if (this.hologramMap.containsKey(name)) {
      return null;
    }

    Hologram hologram = this.hologramFactory.createHologram(location, name);
    this.hologramMap.put(name, hologram);

    UUID worldId = location.getWorld().getUID();
    long chunkKey = UtilChunk.getChunkKey(location);

    this.hologramWorldMap.computeIfAbsent(worldId, key -> new HashMap<>())
            .computeIfAbsent(chunkKey, key -> new ArrayList<>())
            .add(hologram);

    return hologram;
  }

  public void deleteHologram(String hologramID) {
    Hologram hologram = this.hologramMap.remove(hologramID);
    if (hologram == null) {
      return;
    }

    Bukkit.getOnlinePlayers().forEach(hologram::hideFrom);

    Location location = hologram.getLocation();
    UUID worldId = location.getWorld().getUID();
    long chunkKey = UtilChunk.getChunkKey(location);

    Map<Long, List<Hologram>> chunkMap = this.hologramWorldMap.get(worldId);

    if (chunkMap == null) {
      return;
    }

    List<Hologram> holograms = chunkMap.get(chunkKey);

    if (holograms == null) {
      return;
    }

    holograms.remove(hologram);

    if (holograms.isEmpty()) {
      chunkMap.remove(chunkKey);
    }
  }

  public void displayHologramsInChunk(Chunk chunk, Player player) {
    this.forEachHologramInChunk(chunk, hologram -> hologram.showTo(player));
  }

  public void hideHologramsInChunk(Chunk chunk, Player player) {
    this.forEachHologramInChunk(chunk, hologram -> hologram.hideFrom(player));
  }

  private void forEachHologramInChunk(Chunk chunk, Consumer<Hologram> consumer) {
    Map<Long, List<Hologram>> chunkMap = this.hologramWorldMap.get(chunk.getWorld().getUID());
    if (chunkMap == null) {
      return;
    }
    List<Hologram> hologramList = chunkMap.get(chunk.getChunkKey());
    if (hologramList == null) {
      return;
    }
    hologramList.forEach(consumer);
  }

  public List<String> getHologramNames() {
    return List.copyOf(this.hologramMap.keySet());
  }

}
