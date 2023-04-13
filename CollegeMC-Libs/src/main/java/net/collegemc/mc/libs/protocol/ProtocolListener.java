package net.collegemc.mc.libs.protocol;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ProtocolListener implements Listener {

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    ProtocolManager.inject(event.getPlayer());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    ProtocolManager.uninject(event.getPlayer());
  }

}
