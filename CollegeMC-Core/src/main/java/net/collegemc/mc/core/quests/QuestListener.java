package net.collegemc.mc.core.quests;

import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.libs.spigot.events.kill.PlayerKillEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class QuestListener implements Listener {

  @EventHandler
  public void onKillEntity(PlayerKillEntityEvent event) {
    QuestList questList = ActiveCollegeUser.of(event.getPlayer()).getQuestList();
    if (questList == null) {
      return;
    }
    questList.propagateEvent(event);
  }

  @EventHandler
  public void onMineBlock(BlockBreakEvent event) {
    QuestList questList = ActiveCollegeUser.of(event.getPlayer()).getQuestList();
    if (questList == null) {
      return;
    }
    questList.propagateEvent(event);
  }

}
