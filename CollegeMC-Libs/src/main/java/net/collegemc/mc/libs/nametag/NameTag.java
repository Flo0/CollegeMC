package net.collegemc.mc.libs.nametag;

import com.mojang.math.Transformation;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.collegemc.mc.libs.protocol.ProtocolManager;
import net.collegemc.mc.libs.tasks.TaskManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public class NameTag {

  private final Display.TextDisplay displayEntity;
  private final int targetId;
  private final UUID targetUID;
  private ClientboundAddEntityPacket spawnPacket;
  private ClientboundSetEntityDataPacket metaPacket;
  private ClientboundSetPassengersPacket mountPacket;
  private ClientboundRemoveEntitiesPacket destroyPacket;

  public NameTag(Location location, UUID targetUId, int targetId, String display) {
    ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();
    this.displayEntity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
    this.displayEntity.setText(Component.literal(display));
    this.displayEntity.setPos(location.getX(), location.getY(), location.getZ());
    this.displayEntity.setBillboardConstraints(Display.BillboardConstraints.VERTICAL);
    this.displayEntity.setWidth(128);

    Transformation identity = Transformation.identity();
    Vector3f translation = new Vector3f(0, 0.75f, 0);
    Transformation transformation = new Transformation(translation, identity.getLeftRotation(), identity.getScale(), identity.getRightRotation());

    this.displayEntity.setTransformation(transformation);
    this.targetUID = targetUId;
    this.targetId = targetId;
    this.setupPackets();
  }

  public NameTag(Location location, int targetId, String display) {
    this(location, null, targetId, display);
  }

  private void setupPackets() {
    this.createSpawnPacket();
    this.createMetaPacket();
    this.createMountPacket();
    this.createDestroyPacket();
  }

  private void createSpawnPacket() {
    this.spawnPacket = new ClientboundAddEntityPacket(this.displayEntity);
  }

  private void createMetaPacket() {
    List<SynchedEntityData.DataValue<?>> dataValues = this.displayEntity.getEntityData().getNonDefaultValues();
    if (dataValues == null) {
      throw new IllegalStateException("Packed null instead of values.");
    }
    this.metaPacket = new ClientboundSetEntityDataPacket(this.displayEntity.getId(), dataValues);
  }

  private void createMountPacket() {
    IntList passengerIds = new IntArrayList();

    if (this.targetUID != null) {
      Entity entity = Bukkit.getEntity(this.targetUID);
      if (entity != null) {
        entity.getPassengers().forEach(passenger -> passengerIds.add(passenger.getEntityId()));
      }
    }
    passengerIds.add(this.displayEntity.getId());

    ByteBuf parentBuf = Unpooled.buffer();
    FriendlyByteBuf byteBuf = new FriendlyByteBuf(parentBuf);
    byteBuf.writeVarInt(this.targetId);
    byteBuf.writeVarIntArray(passengerIds.toIntArray());
    this.mountPacket = new ClientboundSetPassengersPacket(byteBuf);
  }

  private void createDestroyPacket() {
    this.destroyPacket = new ClientboundRemoveEntitiesPacket(IntList.of(this.displayEntity.getId()));
  }

  public void remountFor(Player player) {
    ProtocolManager.sendTo(player, this.mountPacket);
  }

  public void showTo(Player player) {
    ProtocolManager.sendTo(player, this.spawnPacket);
    ProtocolManager.sendTo(player, this.metaPacket);
    TaskManager.runTaskLaterAsync(() -> ProtocolManager.sendTo(player, this.mountPacket), 1);
  }

  public void hideFrom(Player player) {
    ProtocolManager.sendTo(player, this.destroyPacket);
  }

  public void broadcastShow() {
    ProtocolManager.broadcastPacket(this.spawnPacket);
    ProtocolManager.broadcastPacket(this.metaPacket);
    TaskManager.runTaskLaterAsync(() -> ProtocolManager.broadcastPacket(this.mountPacket), 1);
  }

  private void broadcastMetaChange() {
    ProtocolManager.broadcastPacket(this.metaPacket);
  }

  public void broadCastHide() {
    ProtocolManager.broadcastPacket(this.destroyPacket);
  }

  public void updateDisplay(String display) {
    this.displayEntity.setText(Component.literal(display));
    this.createMetaPacket();
    this.broadcastMetaChange();
  }
}
