package net.collegemc.mc.libs.gui;

import net.collegemc.mc.libs.gui.abstraction.GuiHandler;
import net.collegemc.mc.libs.gui.abstraction.InventoryHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class GuiManager {

  private final Map<Inventory, InventoryHandler> handlerMap = new HashMap<>();

  public GuiManager(JavaPlugin plugin) {
    Bukkit.getPluginManager().registerEvents(new GuiListener(this), plugin);
  }

  public void initializeAndRegister(GuiHandler guiHandler) {
    Inventory inventory = guiHandler.initializeInventory();
    guiHandler.decorate();
    this.registerHandledInventory(inventory, guiHandler);
  }

  public void registerHandledInventory(Inventory inventory, InventoryHandler handler) {
    this.handlerMap.put(inventory, handler);
  }

  public void unregisterInventory(Inventory inventory) {
    this.handlerMap.remove(inventory);
  }

  public InventoryHandler getHandlerOf(Inventory inventory) {
    return this.handlerMap.get(inventory);
  }

  protected void handleClick(InventoryClickEvent event) {
    InventoryHandler handler = this.getHandlerOf(event.getInventory());
    if (handler != null) {
      handler.onClick(event);
    }
  }

  protected void handleOpen(InventoryOpenEvent event) {
    InventoryHandler handler = this.getHandlerOf(event.getInventory());
    if (handler != null) {
      handler.onOpen(event);
    }
  }

  protected void handleClose(InventoryCloseEvent event) {
    Inventory inventory = event.getInventory();
    InventoryHandler handler = this.getHandlerOf(inventory);
    if (handler != null) {
      handler.onClose(event);
      if (handler.unregisterOnClose()) {
        this.unregisterInventory(inventory);
      }
    }
  }

}
