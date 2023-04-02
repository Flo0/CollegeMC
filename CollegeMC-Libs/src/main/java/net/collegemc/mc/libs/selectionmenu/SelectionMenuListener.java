package net.collegemc.mc.libs.selectionmenu;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

public class SelectionMenuListener implements Listener {

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    CollegeLibrary.getSelectionMenuManager().endSelection(event.getPlayer(), true);
  }

  @EventHandler
  public void onSneak(PlayerToggleSneakEvent event) {
    if (event.isSneaking()) {
      return;
    }

    SelectionMenuManager selectionMenuManager = CollegeLibrary.getSelectionMenuManager();
    if (selectionMenuManager.isInSelection(event.getPlayer())) {
      selectionMenuManager.endSelection(event.getPlayer());
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onDismount(EntityDismountEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }
    SelectionMenuManager selectionMenuManager = CollegeLibrary.getSelectionMenuManager();
    if (selectionMenuManager.isInSelection(player)) {
      selectionMenuManager.endSelection(player);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onSwing(PlayerArmSwingEvent event) {
    Player player = event.getPlayer();
    SelectionMenuManager selectionMenuManager = CollegeLibrary.getSelectionMenuManager();
    if (selectionMenuManager.isInSelection(player)) {
      selectionMenuManager.swingHandle(player);
      event.setCancelled(true);
    }
  }

}
