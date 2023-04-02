package net.collegemc.mc.core;

import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.core.active.ActiveCollegeUserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class CollegeCoreListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPreLogin(AsyncPlayerPreLoginEvent event) {
    if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
      return;
    }
    ActiveCollegeUserManager userManager = CollegeCore.getActiveCollegeUserManager();
    userManager.loadData(event.getUniqueId(), event.getName());
  }

  @EventHandler
  public void onLocation(PlayerSpawnLocationEvent event) {
    ActiveCollegeUser.of(event.getPlayer()).getCurrentMetaData().ifPresent(data -> {
      event.setSpawnLocation(data.getLastKnownLocation());
    });
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    ActiveCollegeUser.of(event.getPlayer()).getCurrentMetaData().ifPresent(data -> {
      event.getPlayer().getInventory().setContents(data.getInventoryContent());
    });
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    ActiveCollegeUserManager userManager = CollegeCore.getActiveCollegeUserManager();
    userManager.unloadData(event.getPlayer().getUniqueId());
  }

}
