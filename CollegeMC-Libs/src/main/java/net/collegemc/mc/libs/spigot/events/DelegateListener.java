package net.collegemc.mc.libs.spigot.events;

import net.collegemc.mc.libs.spigot.events.kill.PlayerKillEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class DelegateListener implements Listener {

  @EventHandler
  public void onPlayerKillEntity(EntityDeathEvent event) {
    Entity entity = event.getEntity();
    Player killer = event.getEntity().getKiller();
    if (killer == null) {
      return;
    }
    PlayerKillEntityEvent killEvent = new PlayerKillEntityEvent(killer, entity);
    if (!killEvent.callEvent()) {
      event.setCancelled(true);
    }
  }

}
