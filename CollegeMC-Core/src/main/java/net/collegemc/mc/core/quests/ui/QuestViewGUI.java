package net.collegemc.mc.core.quests.ui;

import net.collegemc.mc.core.quests.Quest;
import net.collegemc.mc.libs.gui.abstraction.GuiButton;
import net.collegemc.mc.libs.gui.baseimpl.DynamicGUI;
import net.collegemc.mc.libs.spigot.ItemBuilder;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class QuestViewGUI extends DynamicGUI {

  private final Quest quest;

  public QuestViewGUI(Quest quest) {
    this.quest = quest;
  }

  @Override
  protected void setupButtons() {
    setButton(4, createQuestButton());
    setButton(7, createBackButton());
  }

  private GuiButton createQuestButton() {
    return GuiButton.builder()
            .iconCreator(quest::getDetailedIcon)
            .eventConsumer(event -> {
            })
            .asyncCreated(false)
            .build();
  }

  private GuiButton createBackButton() {
    return GuiButton.builder()
            .iconCreator(() -> new ItemBuilder(Material.CRAFTING_TABLE)
                    .name(Component.text("§6Back"))
                    .build())
            .eventConsumer(event -> {
              UtilPlayer.playUIClick((Player) event.getWhoClicked());
              new QuestMainGUI((Player) event.getWhoClicked()).openFor((Player) event.getWhoClicked());
            })
            .asyncCreated(false)
            .build();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, InventoryType.DROPPER, Component.text(quest.getId()));
  }
}
