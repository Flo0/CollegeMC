package net.collegemc.mc.libs.gui;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

@RequiredArgsConstructor
public class GuiListener implements Listener {

  private final GuiManager guiManager;

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    this.guiManager.handleClick(event);
  }

  @EventHandler
  public void onOpen(InventoryOpenEvent event) {
    this.guiManager.handleOpen(event);
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    this.guiManager.handleClose(event);
  }

}
