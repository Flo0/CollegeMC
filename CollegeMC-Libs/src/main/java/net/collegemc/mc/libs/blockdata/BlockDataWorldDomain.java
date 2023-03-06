package net.collegemc.mc.libs.blockdata;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BlockDataWorldDomain {

  private final UUID worldID;
  private final Long2ObjectMap<BlockDataChunkDomain> chunkMap = new Long2ObjectOpenHashMap<>();

  public BlockDataWorldDomain(UUID worldID) {
    this.worldID = worldID;
  }

  protected void initChunk(Chunk chunk) {
    BlockDataChunkDomain chunkDomain = new BlockDataChunkDomain(this.worldID, chunk.getChunkKey());
    if (chunkDomain.load()) {
      this.chunkMap.put(chunk.getChunkKey(), chunkDomain);
    }
  }

  protected void terminateChunk(Chunk chunk) {
    Optional.ofNullable(this.chunkMap.get(chunk.getChunkKey())).ifPresent(BlockDataChunkDomain::save);
  }

  protected Optional<PersistentDataContainer> getDataOf(Block block) {
    BlockDataChunkDomain chunkDomain = this.chunkMap.get(block.getChunk().getChunkKey());
    if (chunkDomain == null) {
      return Optional.empty();
    }
    return chunkDomain.getDataOf(block);
  }

  protected PersistentDataContainer createData(Block block) {
    long cKey = block.getChunk().getChunkKey();
    return this.chunkMap.computeIfAbsent(block.getChunk().getChunkKey(), key -> new BlockDataChunkDomain(this.worldID, cKey)).createData(block);
  }

  protected void clearDataOf(Block block) {
    BlockDataChunkDomain chunkDomain = this.chunkMap.get(block.getChunk().getChunkKey());
    if (chunkDomain == null) {
      return;
    }
    chunkDomain.clearDataOf(block);
  }

  public Map<Block, PersistentDataContainer> getDataInChunk(Chunk chunk) {
    long chunkKey = chunk.getChunkKey();
    BlockDataChunkDomain chunkDomain = this.chunkMap.get(chunkKey);
    if (chunkDomain == null) {
      return Collections.emptyMap();
    }
    return chunkDomain.getDataInChunk();
  }

  protected void putData(Block block, PersistentDataContainer container) {
    long cKey = block.getChunk().getChunkKey();
    this.chunkMap.computeIfAbsent(block.getChunk().getChunkKey(), key -> new BlockDataChunkDomain(this.worldID, cKey)).putData(block, container);
  }
}
