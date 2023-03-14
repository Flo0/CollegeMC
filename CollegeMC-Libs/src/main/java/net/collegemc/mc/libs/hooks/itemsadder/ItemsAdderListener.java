package net.collegemc.mc.libs.hooks.itemsadder;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderListener implements Listener {

  @EventHandler
  public void onEnable(ItemsAdderLoadDataEvent event) {
    ItemsAdderHook.loaded = true;
  }

}
