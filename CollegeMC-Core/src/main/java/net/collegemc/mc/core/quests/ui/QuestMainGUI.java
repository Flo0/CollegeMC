package net.collegemc.mc.core.quests.ui;

import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.core.quests.Quest;
import net.collegemc.mc.core.quests.QuestList;
import net.collegemc.mc.libs.gui.abstraction.GuiButton;
import net.collegemc.mc.libs.gui.baseimpl.DynamicGUI;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class QuestMainGUI extends DynamicGUI {

  private final Player player;

  public QuestMainGUI(Player player) {
    this.player = player;
  }

  @Override
  protected void setupButtons() {
    QuestList questList = ActiveCollegeUser.of(player).getQuestList();
    if (questList == null) {
      return;
    }
    questList.getQuests().forEach(quest -> {
      if (quest.isDone()) {
        questList.removeQuest(quest.getId());
      } else {
        addButton(createQuestButton(quest));
      }
    });
  }

  private GuiButton createQuestButton(Quest quest) {
    return GuiButton.builder()
            .iconCreator(quest::getIcon)
            .eventConsumer(event -> {
              event.setCancelled(true);
              UtilPlayer.playUIClick(player);
              new QuestViewGUI(quest).openFor(player);
            })
            .asyncCreated(false)
            .build();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 6 * 9, Component.text("Quests"));
  }
}
