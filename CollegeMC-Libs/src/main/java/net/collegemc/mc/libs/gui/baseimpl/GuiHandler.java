package net.collegemc.mc.libs.gui.baseimpl;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.gui.abstraction.GuiButton;
import net.collegemc.mc.libs.gui.abstraction.InventoryHandler;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract sealed class GuiHandler implements InventoryHandler permits DynamicGUI, StaticGUI {

  private final Map<Integer, GuiButton> buttonMap;

  @Getter
  @Setter
  private boolean autoCanceled = true;
  @Getter
  private boolean decorated = false;
  private Inventory inventory = null;

  protected GuiHandler() {
    this.buttonMap = new HashMap<>();
  }

  public void openFor(Player player) {
    if (this.inventory == null) {
      CollegeLibrary.getGuiManager().initializeAndRegister(this);
    }
    player.openInventory(this.inventory);
  }

  public void setButton(int slot, GuiButton button) {
    this.buttonMap.put(slot, button);
  }

  public void addButton(GuiButton button) {
    for (int index = 0; index < this.inventory.getSize(); index++) {
      if (!this.buttonMap.containsKey(index)) {
        this.setButton(index, button);
        return;
      }
    }
    throw new IllegalStateException("Button does not fit in gui.");
  }

  public Inventory initializeInventory() {
    Preconditions.checkState(this.inventory == null, "Inventory initialisation is only allowed once.");
    this.inventory = this.createInventory();
    return this.inventory;
  }

  public void decorate() {
    Preconditions.checkState(this.inventory != null, "Inventory was not initialized.");
    if (this.decorated) {
      this.inventory.clear();
    }
    this.buttonMap.forEach(this::addToPhysicalInventory);
    this.decorated = true;
  }

  private void addToPhysicalInventory(Integer slot, GuiButton button) {
    Supplier<ItemStack> iconCreator = button.getIconCreator();
    if (iconCreator == null) {
      return;
    }
    if (button.isAsyncCreated()) {
      TaskManager.runOnComputationPool(() -> {
        ItemStack icon = iconCreator.get();
        TaskManager.runTask(() -> this.inventory.setItem(slot, icon));
      });
    } else {
      this.inventory.setItem(slot, iconCreator.get());
    }
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    if (this.inventory.equals(event.getClickedInventory())) {
      this.onTopClick(event);
    } else {
      this.onBottomClick(event);
    }
  }

  public void onTopClick(InventoryClickEvent event) {
    if (this.isAutoCanceled()) {
      event.setCancelled(true);
    }
    GuiButton button = this.buttonMap.get(event.getSlot());
    if (button == null) {
      return;
    }
    button.getEventConsumer().accept(event);
  }

  public void onBottomClick(InventoryClickEvent event) {
    if (this.isAutoCanceled()) {
      event.setCancelled(true);
    }
  }

  protected void clearButtons() {
    this.buttonMap.clear();
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {

  }

  @Override
  public void onClose(InventoryCloseEvent event) {

  }

  protected abstract Inventory createInventory();

}
