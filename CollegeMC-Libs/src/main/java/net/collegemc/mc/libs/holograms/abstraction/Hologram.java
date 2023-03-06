package net.collegemc.mc.libs.holograms.abstraction;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Hologram {

  String getId();

  int size();

  void addLine(String line);

  void setLine(int index, String line);

  String getLine(int index);

  Location getLocation();

  void teleport(Location target);

  void showTo(Player player);

  void hideFrom(Player player);

  void removeLine(int index);
}
