package net.collegemc.mc.libs.tablist;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class TabListListener implements Listener {

  public TabListListener(final TabListManager tablistManager) {
    this.tablistManager = tablistManager;
  }

  private final TabListManager tablistManager;

  @EventHandler(priority = EventPriority.LOWEST)
  public void onJoin(final PlayerJoinEvent event) {
    this.tablistManager.addPlayer(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onQuit(final PlayerQuitEvent event) {
    this.tablistManager.removePlayer(event.getPlayer());
  }

}
