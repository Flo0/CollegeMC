package net.collegemc.mc.libs.skinclient;

import net.collegemc.common.mineskin.data.Skin;

import java.util.HashMap;
import java.util.UUID;

public class PlayerSkinData {

  private final HashMap<String, Integer> namedSkinIds;
  private final HashMap<Integer, Skin> skinMap;

  public PlayerSkinData() {
    this.namedSkinIds = new HashMap<>();
    this.skinMap = new HashMap<>();
  }

  public Skin getSkinByUID(UUID uid) {
    String noDash = uid.toString().replace("-", "");
    return this.skinMap.values().stream().filter(skin -> skin.getUuid().equals(noDash)).findFirst().orElse(null);
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
    this.namedSkinIds.put(skin.getName(), skin.getId());
    this.skinMap.put(skin.getId(), skin);
  }

}
