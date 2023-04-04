package net.collegemc.mc.libs.npcs.abstraction;

import net.collegemc.common.mineskin.data.Skin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.net.URL;

public interface NPC {

  String getInternalName();

  int getEntityId();

  void setPosition(Location location);

  void broadcastPositionChange();

  void lookAt(Location location);

  void setLookDir(float pitch, float yaw);

  void broadcastLookDirChange();

  void showTo(Player player);

  void broadcastShow();

  void hideFrom(Player player);

  void broadcastHide();

  void setSkin(URL skinUrl);

  void setSkin(Skin skin);

  void broadcastSkinUpdate();

  void onClick(Player player);

  void rotate(float angle);

  void broadcastRotationChange();

  void rename(String name);

  void broadcastNameChange();

  Location getLocation();

  boolean isTicked();

  void onTick();

  default boolean isPersistent() {
    return true;
  }

  default boolean isInLoadedChunk() {
    return this.getLocation().isChunkLoaded();
  }

}
