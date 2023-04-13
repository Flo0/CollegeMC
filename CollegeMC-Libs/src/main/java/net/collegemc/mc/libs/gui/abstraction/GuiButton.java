package net.collegemc.mc.libs.gui.abstraction;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@Builder
public class GuiButton {

  private Supplier<ItemStack> iconCreator;
  private Consumer<InventoryClickEvent> eventConsumer;
  private boolean asyncCreated;

}
