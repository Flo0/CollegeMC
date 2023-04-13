package net.collegemc.mc.core.quests.targets;

import net.collegemc.mc.core.quests.QuestTarget;
import net.collegemc.mc.libs.spigot.events.kill.PlayerKillEntityEvent;
import org.bukkit.entity.EntityType;

public class SimpleKillEntityQuest extends QuestTarget<PlayerKillEntityEvent> {

  private final EntityType entityType;

  public SimpleKillEntityQuest(EntityType entityType, int targetProgress) {
    super(PlayerKillEntityEvent.class, targetProgress);
    this.entityType = entityType;
  }

  @Override
  public String getShortDescription() {
    return "Kill " + entityType + " entities";
  }

  @Override
  public Integer apply(PlayerKillEntityEvent playerKillEntityEvent) {
    EntityType type = playerKillEntityEvent.getEntity().getType();
    if (type == entityType) {
      return 1;
    }
    return null;
  }
}
