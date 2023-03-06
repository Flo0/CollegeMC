package net.collegemc.mc.libs.gui.abstraction;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public abstract class GuiHandler implements InventoryHandler {

  private final Map<Integer, GuiButton> buttonMap;

  @Getter
  @Setter
  private boolean autoCanceled = true;
  private boolean decorated = false;
  private Inventory inventory = null;

  protected GuiHandler() {
    this.buttonMap = new HashMap<>();
  }

  public void openFor(Player player) {
    Preconditions.checkState(this.inventory != null, "Inventory was not initialized.");
    Preconditions.checkState(this.decorated, "Inventory was not decorated.");
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
    Mono<ItemStack> iconCreator = button.getIconCreator();
    if (iconCreator == null) {
      return;
    }
    if (button.isAsyncCreated()) {
      iconCreator.subscribe(TaskManager.consumeSync(icon -> this.inventory.setItem(slot, icon)));
    } else {
      this.inventory.setItem(slot, iconCreator.block());
    }
  }

  @Override
  public void onClick(InventoryClickEvent event) {
    if (this.isAutoCanceled()) {
      event.setCancelled(true);
    }
    GuiButton button = this.buttonMap.get(event.getSlot());
    if (button == null) {
      return;
    }
    button.getEventConsumer().accept(event);
  }

  @Override
  public void onOpen(InventoryOpenEvent event) {

  }

  @Override
  public void onClose(InventoryCloseEvent event) {

  }

  protected abstract Inventory createInventory();

}
