package net.collegemc.mc.libs.blockdata;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.collegemc.mc.libs.spigot.UtilChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class BlockDataChunkDomain {

  private final UUID worldID;
  private final long chunkKey;
  private final Int2ObjectMap<PersistentDataContainer> blockDataMap = new Int2ObjectOpenHashMap<>();
  private final NamespacedKey parentKey = Objects.requireNonNull(NamespacedKey.fromString("core:chunk-data-parent"));
  private final NamespacedKey positionKey = Objects.requireNonNull(NamespacedKey.fromString("core:chunk-data-position"));
  private final NamespacedKey dataKey = Objects.requireNonNull(NamespacedKey.fromString("core:chunk-data"));

  protected void save() {
    World world = Bukkit.getWorld(this.worldID);
    if (world == null) {
      return;
    }
    Chunk chunk = world.getChunkAt(this.chunkKey);
    PersistentDataContainer container = chunk.getPersistentDataContainer();
    if (this.blockDataMap.isEmpty()) {
      container.remove(this.parentKey);
      return;
    }
    PersistentDataContainer[] dataContainers = new PersistentDataContainer[this.blockDataMap.size()];
    AtomicInteger counter = new AtomicInteger();
    this.blockDataMap.int2ObjectEntrySet().forEach(entry -> {
      PersistentDataContainer mapContainer = container.getAdapterContext().newPersistentDataContainer();
      mapContainer.set(this.positionKey, PersistentDataType.INTEGER, entry.getIntKey());
      mapContainer.set(this.dataKey, PersistentDataType.TAG_CONTAINER, entry.getValue());
      dataContainers[counter.getAndIncrement()] = mapContainer;
    });
    container.set(this.parentKey, PersistentDataType.TAG_CONTAINER_ARRAY, dataContainers);
  }

  protected boolean load() {
    World world = Bukkit.getWorld(this.worldID);
    if (world == null) {
      return false;
    }
    Chunk chunk = world.getChunkAt(this.chunkKey);
    PersistentDataContainer container = chunk.getPersistentDataContainer();
    PersistentDataContainer[] dataContainers = container.get(this.parentKey, PersistentDataType.TAG_CONTAINER_ARRAY);
    if (dataContainers == null || dataContainers.length == 0) {
      return false;
    }
    for (PersistentDataContainer dataContainer : dataContainers) {
      Integer pos = dataContainer.get(this.positionKey, PersistentDataType.INTEGER);
      if (pos == null) {
        continue;
      }
      PersistentDataContainer data = dataContainer.get(this.dataKey, PersistentDataType.TAG_CONTAINER);
      if (data == null) {
        continue;
      }
      this.blockDataMap.put(pos.intValue(), data);
    }
    return true;
  }

  protected Optional<PersistentDataContainer> getDataOf(Block block) {
    return Optional.ofNullable(this.blockDataMap.get(this.keyOf(block)));
  }

  protected PersistentDataContainer createData(Block block) {
    PersistentDataAdapterContext context = block.getChunk().getPersistentDataContainer().getAdapterContext();
    return this.blockDataMap.computeIfAbsent(this.keyOf(block), key -> context.newPersistentDataContainer());
  }

  protected PersistentDataContainer clearDataOf(Block block) {
    return this.blockDataMap.remove(this.keyOf(block));
  }

  protected Map<Block, PersistentDataContainer> getDataInChunk() {
    Map<Block, PersistentDataContainer> data = new HashMap<>();
    World world = Bukkit.getWorld(this.worldID);
    if (world == null) {
      return Collections.emptyMap();
    }

    Chunk chunk = world.getChunkAt(this.chunkKey);
    this.blockDataMap.int2ObjectEntrySet().forEach(entry -> {
      int key = entry.getIntKey();
      int x = this.keyToX(key);
      int y = this.keyToY(key);
      int z = this.keyToZ(key);
      Block block = chunk.getBlock(x, y, z);
      PersistentDataContainer container = entry.getValue();
      data.put(block, container);
    });

    return data;
  }

  protected void putData(Block block, PersistentDataContainer container) {
    this.blockDataMap.put(this.keyOf(block), container);
  }

  private int keyOf(Block block) {
    return UtilChunk.relativeKeyOf(block);
  }

  private int keyToX(int key) {
    return (key >> 16) & 0xFF;
  }

  private int keyToY(int key) {
    return key & 0xFFFF;
  }

  private int keyToZ(int key) {
    return (key >> 24) & 0xFF;
  }

}
