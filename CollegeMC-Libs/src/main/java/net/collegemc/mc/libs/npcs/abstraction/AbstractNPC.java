package net.collegemc.mc.libs.npcs.abstraction;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import it.unimi.dsi.fastutil.ints.IntList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.Vector;

import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractNPC implements NPC {

  private final int entityId;
  private final UUID playerId;
  private final String displayName;
  private final String internalName;
  private final OfflinePlayer offlinePlayer;
  private Location location;

  private PacketContainer infoUpdatePacket;
  private PacketContainer spawnPacket;
  private PacketContainer infoRemovePacket;
  private PacketContainer despawnPacket;
  private PacketContainer teleportPacket;
  private PacketContainer rotationPacket;
  private PacketContainer positionPacket;

  public AbstractNPC(Location location, String internalName, String displayName) {
    this.entityId = ThreadLocalRandom.current().nextInt();
    this.internalName = internalName;
    this.displayName = displayName;
    this.location = location;
    this.offlinePlayer = Bukkit.getOfflinePlayer(UUID.randomUUID());
    this.playerId = this.offlinePlayer.getUniqueId();
    this.setupPackets();
  }

  private void setupPackets() {
    this.setupInfoPacket();
    this.setupSpawnPacket();
    this.setupInfoRemovePacket();
    this.setupDespawnPacket();
    this.setupRotationPackets();
  }

  private void setupInfoPacket() {
    this.infoUpdatePacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);

    // Setting actions
    EnumSet<EnumWrappers.PlayerInfoAction> actions = EnumSet.of(EnumWrappers.PlayerInfoAction.ADD_PLAYER);
    this.infoUpdatePacket.getPlayerInfoActions().write(0, actions);

    // Setting entries
    WrappedGameProfile wrappedGameProfile = WrappedGameProfile.fromOfflinePlayer(this.offlinePlayer).withName(this.internalName);
    int latency = 0;
    EnumWrappers.NativeGameMode gameMode = EnumWrappers.NativeGameMode.CREATIVE;
    WrappedChatComponent displayName = WrappedChatComponent.fromLegacyText(this.displayName);

    List<PlayerInfoData> infoDataList = new ArrayList<>();
    PlayerInfoData addData = new PlayerInfoData(wrappedGameProfile, latency, gameMode, displayName);
    infoDataList.add(addData);

    this.infoUpdatePacket.getPlayerInfoDataLists().write(1, infoDataList);
  }

  private void setupSpawnPacket() {
    this.spawnPacket = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);

    this.spawnPacket.getIntegers().write(0, this.entityId);
    this.spawnPacket.getUUIDs().write(0, this.playerId);

    this.spawnPacket.getDoubles()
            .write(0, this.location.getX())
            .write(1, this.location.getY())
            .write(2, this.location.getZ());

    byte yRot = (byte) ((int) (this.location.getYaw() * (256.0F / 360.0F)));
    byte xRot = (byte) ((int) (this.location.getPitch() * (256.0F / 360.0F)));

    this.spawnPacket.getBytes()
            .write(0, yRot)
            .write(1, xRot);
  }

  private void setupInfoRemovePacket() {
    this.infoRemovePacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO_REMOVE);
    this.infoRemovePacket.getUUIDLists().write(0, List.of(this.playerId));
  }

  private void setupDespawnPacket() {
    this.despawnPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
    this.despawnPacket.getIntLists().write(0, IntList.of(this.entityId));
  }

  private void setupTeleportPacket() {
    this.teleportPacket = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

    this.teleportPacket.getIntegers().write(0, this.entityId);

    this.teleportPacket.getDoubles()
            .write(0, this.location.getX())
            .write(1, this.location.getY())
            .write(2, this.location.getZ());

    byte yRot = (byte) ((int) (this.location.getYaw() * (256.0F / 360.0F)));
    byte xRot = (byte) ((int) (this.location.getPitch() * (256.0F / 360.0F)));

    this.teleportPacket.getBytes()
            .write(0, yRot)
            .write(1, xRot);

    this.teleportPacket.getBooleans().write(0, true);
  }

  private void setupRotationPackets() {
    this.rotationPacket = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);

    this.rotationPacket.getIntegers().write(0, this.entityId);

    byte yRot = (byte) ((int) (this.location.getYaw() * (256.0F / 360.0F)));
    byte xRot = (byte) ((int) (this.location.getPitch() * (256.0F / 360.0F)));

    this.rotationPacket.getBytes()
            .write(0, yRot);

    this.positionPacket = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);

    this.positionPacket.getIntegers().write(0, this.entityId);

    this.positionPacket.getShorts()
            .write(0, (short) 0)
            .write(1, (short) 0)
            .write(2, (short) 0);

    this.positionPacket.getBytes()
            .write(0, yRot)
            .write(1, xRot);

    this.positionPacket.getBooleans()
            .write(0, true)
            .write(1, true)
            .write(2, false);
  }

  private Location getEyeLocation() {
    return this.location.clone().add(0, 1.75, 0);
  }

  @Override
  public String getInternalName() {
    return this.internalName;
  }

  @Override
  public int getEntityId() {
    return this.entityId;
  }

  @Override
  public void setPosition(Location location) {
    this.location = location;
    this.setupTeleportPacket();
  }

  @Override
  public void broadcastPositionChange() {
    ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    protocolManager.broadcastServerPacket(this.teleportPacket);
  }

  @Override
  public void lookAt(Location location) {
    Vector lookDir = location.toVector().subtract(this.getEyeLocation().toVector());
    this.location.setDirection(lookDir);
    this.setupRotationPackets();
  }

  @Override
  public void broadcastRotationChange() {
    ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    protocolManager.broadcastServerPacket(this.positionPacket);
    protocolManager.broadcastServerPacket(this.rotationPacket);
  }

  @Override
  public void showTo(Player player) {
    ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    protocolManager.sendServerPacket(player, this.infoUpdatePacket);
    protocolManager.sendServerPacket(player, this.spawnPacket);
  }

  @Override
  public void hideFrom(Player player) {
    ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    protocolManager.sendServerPacket(player, this.despawnPacket);
    protocolManager.sendServerPacket(player, this.infoRemovePacket);
  }

  @Override
  public void setSkin(URL skinUrl) {
    PlayerProfile playerProfile = this.offlinePlayer.getPlayerProfile();
    PlayerTextures textures = playerProfile.getTextures();
    textures.setSkin(skinUrl);
    playerProfile.setTextures(textures);
  }

  @Override
  public void broadcastSkinUpdate() {
    Bukkit.getOnlinePlayers().forEach(player -> {
      this.hideFrom(player);
      this.showTo(player);
    });
  }

  @Override
  public Location getLocation() {
    return this.location;
  }
}
