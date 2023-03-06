package net.collegemc.mc.libs.blockdata;

import com.destroystokyo.paper.event.block.TNTPrimeEvent;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public record BlockDataListener(BlockDataManager blockDataManager) implements Listener {

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onWorldLoad(WorldLoadEvent event) {
    this.blockDataManager.initWorld(event.getWorld());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onWorldUnLoad(WorldUnloadEvent event) {
    this.blockDataManager.terminateWorld(event.getWorld());
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onChunkLoad(ChunkLoadEvent event) {
    this.blockDataManager.initChunk(event.getChunk());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onChunkUnload(ChunkUnloadEvent event) {
    this.blockDataManager.terminateChunk(event.getChunk());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBreak(BlockBreakEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBurn(BlockBurnEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onExplode(BlockExplodeEvent event) {
    event.blockList().forEach(this.blockDataManager::terminateBlock);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onExplode(EntityExplodeEvent event) {
    event.blockList().forEach(this.blockDataManager::terminateBlock);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onFade(BlockFadeEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onForm(BlockFormEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onFromTo(BlockFromToEvent event) {
    this.blockDataManager.terminateBlock(event.getToBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onGrow(BlockGrowEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onMultiPlace(BlockMultiPlaceEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
    event.getReplacedBlockStates().stream().map(BlockState::getBlock).forEach(this.blockDataManager::terminateBlock);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onExtend(BlockPistonExtendEvent event) {
    this.blockDataManager.shiftDataInDirection(event.getDirection(), event.getBlocks());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onRetract(BlockPistonRetractEvent event) {
    this.blockDataManager.shiftDataInDirection(event.getDirection(), event.getBlocks());
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onSpread(BlockSpreadEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onForm(EntityBlockFormEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onDecay(LeavesDecayEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBreak(BlockBreakBlockEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPrime(TNTPrimeEvent event) {
    this.blockDataManager.terminateBlock(event.getBlock());
  }

}
