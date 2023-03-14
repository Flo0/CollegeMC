package net.collegemc.mc.libs.npcs.listener;

import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class NPCListener implements Listener {

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    CollegeLibrary.getNpcManager().sendAllNPCsAsync(event.getPlayer());
  }

}
