package net.collegemc.mc.libs.skinclient;

import org.mineskin.data.Skin;

import java.util.HashMap;

public class PlayerSkinData {

  private final HashMap<String, Integer> namedSkinIds;
  private final HashMap<Integer, Skin> skinMap;

  public PlayerSkinData() {
    this.namedSkinIds = new HashMap<>();
    this.skinMap = new HashMap<>();
  }

  public Integer getSkinId(String skinName) {
    return this.namedSkinIds.get(skinName);
  }

  public Skin getSkin(int skinId) {
    return this.skinMap.get(skinId);
  }

  public Skin getSkin(String skinName) {
    return this.getSkin(this.namedSkinIds.getOrDefault(skinName, 0));
  }

  public void addSkin(Skin skin) {
    this.namedSkinIds.put(skin.name, skin.id);
    this.skinMap.put(skin.id, skin);
  }

}
