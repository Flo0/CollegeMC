package net.collegemc.mc.libs.gui.util;

import net.collegemc.mc.libs.gui.abstraction.GuiButton;
import net.collegemc.mc.libs.gui.baseimpl.DynamicGUI;
import net.collegemc.mc.libs.spigot.ItemBuilder;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.collegemc.mc.libs.tasks.TaskManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;

public class ConfirmationGUI extends DynamicGUI {

  private final Consumer<Boolean> consumer;
  private final String text;

  public ConfirmationGUI(Consumer<Boolean> consumer, String text) {
    this.consumer = consumer;
    this.text = text;
  }

  @Override
  protected void setupButtons() {
    this.setButton(2, this.createAcceptButton());
    this.setButton(6, this.createDenyButton());
  }

  private GuiButton createAcceptButton() {
    return GuiButton.builder()
            .eventConsumer(event -> {
              TaskManager.runTask(() -> this.consumer.accept(true));
              UtilPlayer.playUIClick((Player) event.getWhoClicked());
            }).iconCreator(() -> new ItemBuilder(Material.GREEN_WOOL)
                    .name("§aYes")
                    .build())
            .build();
  }

  private GuiButton createDenyButton() {
    return GuiButton.builder()
            .eventConsumer(event -> {
              TaskManager.runTask(() -> this.consumer.accept(false));
              UtilPlayer.playUIClick((Player) event.getWhoClicked());
            }).iconCreator(() -> new ItemBuilder(Material.RED_WOOL)
                    .name("§cNo")
                    .build())
            .build();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 9, Component.text(this.text));
  }

  @Override
  public void onClose(InventoryCloseEvent event) {
    TaskManager.runTask(() -> this.consumer.accept(false));
    super.onClose(event);
  }
}
