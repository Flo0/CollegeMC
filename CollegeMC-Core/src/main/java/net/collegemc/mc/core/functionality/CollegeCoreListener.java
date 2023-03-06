package net.collegemc.mc.core.functionality;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;

public class CollegeCoreListener implements Listener {

  @EventHandler
  public void onPreLogin(AsyncPlayerPreLoginEvent event) {
    CollegeCore.getActiveCollegeUserManager().load(event.getUniqueId(), event.getName());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    CompletableFuture.runAsync(() -> CollegeCore.getActiveCollegeUserManager().unload(event.getPlayer().getUniqueId()));
  }

}
