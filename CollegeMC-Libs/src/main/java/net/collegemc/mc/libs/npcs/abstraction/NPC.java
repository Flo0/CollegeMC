package net.collegemc.mc.libs.npcs.abstraction;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.net.URL;

public interface NPC {

  String getInternalName();

  int getEntityId();

  void setPosition(Location location);

  void broadcastPositionChange();

  void lookAt(Location location);

  void broadcastRotationChange();

  void showTo(Player player);

  void hideFrom(Player player);

  void setSkin(URL skinUrl);

  void broadcastSkinUpdate();

  void onClick(Player player);

  Location getLocation();

  boolean isTicked();

  void onTick();

  default boolean isInLoadedChunk() {
    return this.getLocation().isChunkLoaded();
  }

}
