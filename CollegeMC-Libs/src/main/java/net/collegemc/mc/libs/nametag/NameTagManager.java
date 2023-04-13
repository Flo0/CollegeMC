package net.collegemc.mc.libs.nametag;

import net.collegemc.mc.libs.protocol.ProtocolManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class NameTagManager {

  private final Map<Integer, NameTag> activeTags;

  public NameTagManager(JavaPlugin plugin) {
    this.activeTags = new HashMap<>();
    Bukkit.getPluginManager().registerEvents(new NameTagListener(), plugin);
    ProtocolManager.registerPacketHandler(new NameTagDespawnPacketHandler());
    ProtocolManager.registerPacketHandler(new NameTagSpawnPacketHandler());
    ProtocolManager.registerPacketHandler(new NameTagPlayerPacketHandler());
    ProtocolManager.registerPacketHandler(new NameTagTeleportPacketHandler());
  }

  public void tagVirtual(Location location, int entityId, String name) {
    if (this.isTagged(entityId)) {
      this.activeTags.get(entityId).updateDisplay(name);
    } else {
      NameTag nameTag = new NameTag(location, entityId, name);
      this.activeTags.put(entityId, nameTag);
    }
  }

  public void tag(Entity entity, String name) {
    if (this.isTagged(entity.getEntityId())) {
      this.activeTags.get(entity.getEntityId()).updateDisplay(name);
    } else {
      NameTag nameTag = new NameTag(entity.getLocation(), entity.getUniqueId(), entity.getEntityId(), name);
      this.activeTags.put(entity.getEntityId(), nameTag);
      nameTag.broadcastShow();
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
