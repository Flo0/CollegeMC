package net.collegemc.mc.core.profileselection;

import net.collegemc.mc.libs.npcs.abstraction.AbstractNPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProfileDisplayNPC extends AbstractNPC {

  private static int counter = 0;

  public ProfileDisplayNPC(Location location, String displayName) {
    super(location, "_DISPLAY_" + (counter++), displayName);
  }

  @Override
  public void onClick(Player player) {

  }

  @Override
  public boolean isTicked() {
    return true;
  }

  @Override
  public void onTick() {
    this.rotate(3);
    this.matchHeadAndBodyRotation();
    this.broadcastRotationChange();
    this.broadcastLookDirChange();
  }

  @Override
  public boolean isPersistent() {
    return false;
  }

}
