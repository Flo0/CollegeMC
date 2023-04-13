package net.collegemc.mc.core.quests.targets;

import net.collegemc.mc.core.quests.QuestTarget;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;

public class SimpleBlockBreakTarget extends QuestTarget<BlockBreakEvent> {

  private final Material blockType;

  public SimpleBlockBreakTarget(Material blockType, int amount) {
    super(BlockBreakEvent.class, amount);
    this.blockType = blockType;
  }

  @Override
  public String getShortDescription() {
    return "Break " + blockType + " blocks";
  }

  @Override
  public Integer apply(BlockBreakEvent blockBreakEvent) {
    Material material = blockBreakEvent.getBlock().getType();
    return material == blockType ? 1 : null;
  }
}
