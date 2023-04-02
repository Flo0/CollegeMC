package net.collegemc.mc.libs.nametag;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class NameTagManager {

  private final Map<Integer, NameTag> activeTags;

  public NameTagManager() {
    this.activeTags = new HashMap<>();
  }

  public void tagVirtual(Location location, int entityId, String name) {
    if (this.isTagged(entityId)) {
      this.activeTags.get(entityId).updateDisplay(name);
    } else {
      NameTag nameTag = new NameTag(location, entityId, name);
      nameTag.broadcastShow();
      this.activeTags.put(entityId, nameTag);
    }
  }

  public void tag(Entity entity, String name) {
    if (this.isTagged(entity.getEntityId())) {
      this.activeTags.get(entity.getEntityId()).updateDisplay(name);
    } else {
      NameTag nameTag = new NameTag(entity.getLocation(), entity.getUniqueId(), entity.getEntityId(), name);
      nameTag.broadcastShow();
      this.activeTags.put(entity.getEntityId(), nameTag);
    }
  }

  public NameTag getTag(int entityId) {
    return this.activeTags.get(entityId);
  }

  public boolean isTagged(int entityId) {
    return this.activeTags.containsKey(entityId);
  }

  public void untag(int entityId) {
    NameTag tag = this.activeTags.remove(entityId);
    if (tag != null) {
      tag.broadCastHide();
    }
  }

}
