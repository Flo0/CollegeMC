package net.collegemc.mc.libs.npcs;

import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.npcs.abstraction.AbstractNPC;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Comparator;

public class IdleNPC extends AbstractNPC {
  public IdleNPC(Location location, String internalName, String displayName) {
    super(location, internalName, displayName);
  }

  @Override
  public void onClick(Player player) {
    Msg.sendInfo(player, "You have clicked the NPC {}.", this.getInternalName());
    player.playSound(this.getLocation(), Sound.ENTITY_VILLAGER_YES, 1F, 1F);
  }

  @Override
  public boolean isTicked() {
    return true;
  }

  @Override
  public void onTick() {
    this.getLocation().getNearbyPlayers(5).stream()
            .min(Comparator.comparingDouble(pl -> pl.getLocation().distanceSquared(this.getLocation())))
            .ifPresent(pl -> {
              this.lookAt(pl.getEyeLocation());
              this.broadcastLookDirChange();
              this.broadcastRotationChange();
            });
  }
}
