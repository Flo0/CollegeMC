package net.collegemc.mc.libs.selectionmenu.baseimpl;

import net.collegemc.mc.libs.selectionmenu.TieDown;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class SpectatingLockDown implements TieDown {

  private final ArmorStand viewStand;
  private GameMode previousGameMode = GameMode.ADVENTURE;

  public SpectatingLockDown(Location location) {
    this.viewStand = location.getWorld().spawn(location, ArmorStand.class, as -> {
      as.setVisible(false);
      as.setGravity(false);
      as.setInvulnerable(true);
      as.setPersistent(false);
    });
  }

  @Override
  public void tieDown(Player player, Location location) {
    this.previousGameMode = player.getPreviousGameMode();
    player.setGameMode(GameMode.SPECTATOR);
    player.setSpectatorTarget(this.viewStand);
  }

  @Override
  public void release(Player player) {
    player.setSpectatorTarget(null);
    player.setGameMode(this.previousGameMode);
    this.viewStand.remove();
  }

}
