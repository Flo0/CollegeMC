package net.collegemc.mc.libs.npcs.abstraction;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.IntList;
import net.collegemc.common.gson.PostDeserializationReactor;
import net.collegemc.common.mineskin.data.Skin;
import net.collegemc.common.mineskin.data.Texture;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.nametag.NameTag;
import net.collegemc.mc.libs.protocol.ProtocolManager;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.util.Vector;

import java.net.URL;
import java.util.List;
import java.util.UUID;

public abstract class AbstractNPC implements NPC, PostDeserializationReactor {

  private final ServerPlayer serverPlayer;
  private String displayName;
  private final String internalName;

  private transient ClientboundPlayerInfoUpdatePacket infoUpdatePacket;
  private transient ClientboundAddPlayerPacket spawnPacket;
  private transient ClientboundPlayerInfoRemovePacket infoRemovePacket;
  private transient ClientboundRemoveEntitiesPacket despawnPacket;
  private transient ClientboundTeleportEntityPacket teleportPacket;
  private transient ClientboundRotateHeadPacket lookAtPacket;
  private transient ClientboundMoveEntityPacket rotationPacket;

  public AbstractNPC(Location location, String internalName, String displayName) {
    GameProfile gameProfile = new GameProfile(UUID.randomUUID(), internalName);
    ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();
    this.serverPlayer = new ServerPlayer(MinecraftServer.getServer(), world, gameProfile);
    this.serverPlayer.setPos(location.getX(), location.getY(), location.getZ());
    this.internalName = internalName;
    this.displayName = displayName;
    this.setupPackets();
  }

  private void setupPackets() {
    this.setupInfoPacket();
    this.setupSpawnPacket();
    this.setupInfoRemovePacket();
    this.setupDespawnPacket();
    this.setupLookPacket();
    this.setupRotationPacket();
  }

  private void setupInfoPacket() {
    ClientboundPlayerInfoUpdatePacket.Action action = ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER;
    this.infoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(action, this.serverPlayer);
  }

  private void setupSpawnPacket() {
    this.spawnPacket = new ClientboundAddPlayerPacket(this.serverPlayer);
  }

  private void setupInfoRemovePacket() {
    this.infoRemovePacket = new ClientboundPlayerInfoRemovePacket(List.of(this.serverPlayer.getUUID()));
  }

  private void setupDespawnPacket() {
    this.despawnPacket = new ClientboundRemoveEntitiesPacket(IntList.of(this.serverPlayer.getId()));
  }

  private void setupTeleportPacket() {
    this.teleportPacket = new ClientboundTeleportEntityPacket(this.serverPlayer);
  }

  private void setupLookPacket() {
    byte yRot = (byte) ((int) (this.serverPlayer.getYHeadRot() * (256.0F / 360.0F)));
    this.lookAtPacket = new ClientboundRotateHeadPacket(this.serverPlayer, yRot);
  }

  private void setupRotationPacket() {
    int id = this.serverPlayer.getId();
    byte yRot = (byte) ((int) (this.serverPlayer.getYRot() * (256.0F / 360.0F)));
    byte xRot = (byte) ((int) (this.serverPlayer.getXRot() * (256.0F / 360.0F)));
    boolean onGround = true;
    this.rotationPacket = new ClientboundMoveEntityPacket.Rot(id, yRot, xRot, onGround);
  }

  @Override
  public String getInternalName() {
    return this.internalName;
  }

  @Override
  public int getEntityId() {
    return this.serverPlayer.getId();
  }

  @Override
  public void setPosition(Location location) {
    this.serverPlayer.setPos(location.getX(), location.getY(), location.getZ());
    this.setupTeleportPacket();
  }

  @Override
  public void broadcastPositionChange() {
    ProtocolManager.broadcastPacket(this.teleportPacket);
  }

  @Override
  public void lookAt(Location location) {
    CraftPlayer player = this.serverPlayer.getBukkitEntity();
    Vector direction = location.toVector().subtract(player.getEyeLocation().toVector());
    location.setDirection(direction);
    this.serverPlayer.setYHeadRot(location.getYaw());
    this.serverPlayer.setYRot(location.getYaw());
    this.serverPlayer.setXRot(location.getPitch());
    this.setupLookPacket();
    this.setupRotationPacket();
  }

  @Override
  public void setLookDir(float pitch, float yaw) {
    this.serverPlayer.setYHeadRot(yaw);
    this.serverPlayer.setXRot(pitch);
    this.serverPlayer.setYRot(yaw);
    this.setupLookPacket();
  }

  public void setLookYaw(float yaw) {
    this.setLookDir(this.serverPlayer.getXRot(), yaw);
  }

  public void matchHeadAndBodyRotation() {
    this.setLookYaw(this.serverPlayer.getYRot());
  }

  @Override
  public void broadcastLookDirChange() {
    ProtocolManager.broadcastPacket(this.lookAtPacket);
  }

  @Override
  public void showTo(Player player) {
    ProtocolManager.sendTo(player, this.infoUpdatePacket);
    ProtocolManager.sendTo(player, this.spawnPacket);
    this.broadcastNameChange();
  }

  @Override
  public void broadcastShow() {
    ProtocolManager.broadcastPacket(this.infoUpdatePacket);
    ProtocolManager.broadcastPacket(this.spawnPacket);
    this.broadcastNameChange();
  }

  @Override
  public void hideFrom(Player player) {
    ProtocolManager.sendTo(player, this.despawnPacket);
    ProtocolManager.sendTo(player, this.infoRemovePacket);
    NameTag tag = CollegeLibrary.getNameTagManager().getTag(this.serverPlayer.getId());
    if(tag != null) {
      tag.hideFrom(player);
    }
  }

  @Override
  public void broadcastHide() {
    ProtocolManager.broadcastPacket(this.despawnPacket);
    ProtocolManager.broadcastPacket(this.infoRemovePacket);
    CollegeLibrary.getNameTagManager().untag(this.serverPlayer.getId());
  }

  @Override
  public void setSkin(URL skinUrl) {
    PlayerProfile playerProfile = this.serverPlayer.getBukkitEntity().getPlayerProfile();
    PlayerTextures textures = playerProfile.getTextures();
    textures.setSkin(skinUrl);
    playerProfile.setTextures(textures);
  }

  @Override
  public void setSkin(Skin skin) {
    PlayerProfile playerProfile = this.serverPlayer.getBukkitEntity().getPlayerProfile();
    playerProfile.removeProperty("textures");
    Texture texture = skin.getData().getTexture();
    playerProfile.setProperty(new ProfileProperty("textures", texture.getValue(), texture.getSignature()));
    serverPlayer.getBukkitEntity().setPlayerProfile(playerProfile);
  }

  @Override
  public void broadcastSkinUpdate() {
    Bukkit.getOnlinePlayers().forEach(player -> {
      this.hideFrom(player);
      this.showTo(player);
    });
  }

  @Override
  public void rotate(float angle) {
    this.serverPlayer.setYRot((this.serverPlayer.getYRot() + angle) % 360);
    this.setupRotationPacket();
  }

  @Override
  public void broadcastRotationChange() {
    ProtocolManager.broadcastPacket(this.rotationPacket);
  }

  @Override
  public void rename(String name) {
    this.displayName = name;
  }

  @Override
  public void broadcastNameChange() {
    CollegeLibrary.getNameTagManager().tagVirtual(this.getLocation(), this.serverPlayer.getId(), this.displayName);
  }

  @Override
  public Location getLocation() {
    return this.serverPlayer.getBukkitEntity().getLocation();
  }

  @Override
  public void postDeserialization() {
    this.setupPackets();
    this.broadcastNameChange();
  }
}
