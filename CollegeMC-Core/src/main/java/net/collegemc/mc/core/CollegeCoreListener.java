package net.collegemc.mc.core;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CollegeCoreListener implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPreLogin(AsyncPlayerPreLoginEvent event) {
    if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
      return;
    }
    CollegeCore.getActiveCollegeUserManager().load(event.getUniqueId(), event.getName());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    CollegeCore.getActiveCollegeUserManager().unload(event.getPlayer().getUniqueId());
  }

}
