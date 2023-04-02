package net.collegemc.mc.libs.protocol;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ProtocolListener implements Listener {

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    ProtocolManager.inject(event.getPlayer());
  }

}
