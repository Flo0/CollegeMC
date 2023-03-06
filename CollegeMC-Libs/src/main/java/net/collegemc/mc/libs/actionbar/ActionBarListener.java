package net.collegemc.mc.libs.actionbar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ActionBarListener implements Listener {

  public ActionBarListener(final ActionBarManager actionBarManager) {
    this.actionBarManager = actionBarManager;
  }

  private final ActionBarManager actionBarManager;

  @EventHandler(priority = EventPriority.LOWEST)
  public void onJoin(final PlayerJoinEvent event) {
    this.actionBarManager.init(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onQuit(final PlayerQuitEvent event) {
    this.actionBarManager.terminate(event.getPlayer());
  }

}
