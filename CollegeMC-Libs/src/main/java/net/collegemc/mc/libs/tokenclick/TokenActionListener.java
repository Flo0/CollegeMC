package net.collegemc.mc.libs.tokenclick;

import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class TokenActionListener implements Listener {

  private final TokenActionManager tokenActionManager;

  @EventHandler
  public void onJoin(final PlayerJoinEvent event) {
    this.tokenActionManager.addPlayer(event.getPlayer());
  }

  @EventHandler
  public void onQuit(final PlayerQuitEvent event) {
    this.tokenActionManager.removePlayer(event.getPlayer());
  }

}
