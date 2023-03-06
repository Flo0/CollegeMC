package net.collegemc.mc.libs.holograms.abstraction;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface HologramLine {

  String getText();

  void showTo(Player player);

  void hideFrom(Player player);

  void teleport(Location location);

  void setText(String text);

  void updateTextFor(Player player);

  void updateLocationFor(Player player);

}
