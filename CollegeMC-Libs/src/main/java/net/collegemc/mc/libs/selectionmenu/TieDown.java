package net.collegemc.mc.libs.selectionmenu;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface TieDown {

  void tieDown(Player player, Location location);

  void release(Player player);

}
