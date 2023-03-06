package net.collegemc.mc.libs.gui.abstraction;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Getter
@Builder
public class GuiButton {

  private Mono<ItemStack> iconCreator;
  private Consumer<InventoryClickEvent> eventConsumer;
  private boolean asyncCreated;

}
