package net.collegemc.mc.libs.gui.abstraction;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public interface InventoryHandler {

  void onClick(InventoryClickEvent event);

  void onOpen(InventoryOpenEvent event);

  void onClose(InventoryCloseEvent event);

  default boolean unregisterOnClose() {
    return true;
  }

}
