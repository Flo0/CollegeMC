package net.collegemc.mc.libs.holograms.implementations.nms;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntList;
import net.collegemc.mc.libs.holograms.abstraction.HologramLine;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Objects;

public class NMSHologramLine implements HologramLine {

  private final ClientboundRemoveEntitiesPacket removePacket;
  private final ClientboundAddEntityPacket addPacket;
  private final ArmorStand nmsArmorStandBackbone;
  private ClientboundTeleportEntityPacket teleportPacket;
  private ClientboundSetEntityDataPacket dataPacket;
  private String currentText;

  public NMSHologramLine(Location loc) {
    World world = loc.getWorld();
    Preconditions.checkArgument(world != null);
    Level level = ((CraftWorld) world).getHandle();
    this.nmsArmorStandBackbone = new ArmorStand(level, loc.getX(), loc.getY(), loc.getZ());
    this.nmsArmorStandBackbone.setMarker(true);
    this.nmsArmorStandBackbone.setCustomNameVisible(true);
    this.nmsArmorStandBackbone.setInvisible(true);

    this.removePacket = this.createRemovePacket();
    this.addPacket = this.createAddPacket();
    this.teleportPacket = this.createMovePacket();
    this.dataPacket = this.createDataPacket();
  }

  @Override
  public String getText() {
    return this.currentText;
  }

  @Override
  public void setText(String text) {
    this.currentText = text;
    this.nmsArmorStandBackbone.setCustomName(Component.literal(text.replace("&", "§")));
    this.dataPacket = this.createDataPacket();
  }

  @Override
  public void showTo(Player player) {
    ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
    connection.send(this.addPacket);
    connection.send(this.dataPacket);
  }

  @Override
  public void hideFrom(Player player) {
    ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
    connection.send(this.removePacket);
  }

  @Override
  public void teleport(Location location) {
    this.nmsArmorStandBackbone.setPos(location.getX(), location.getY(), location.getZ());
    this.teleportPacket = this.createMovePacket();
  }

  @Override
  public void updateTextFor(Player player) {
    ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
    connection.send(this.dataPacket);
  }

  @Override
  public void updateLocationFor(Player player) {
    ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
    connection.send(this.teleportPacket);
  }

  private ClientboundAddEntityPacket createAddPacket() {
    return new ClientboundAddEntityPacket(this.nmsArmorStandBackbone);
  }

  private ClientboundSetEntityDataPacket createDataPacket() {
    SynchedEntityData data = this.nmsArmorStandBackbone.getEntityData();
    return new ClientboundSetEntityDataPacket(this.nmsArmorStandBackbone.getId(), Objects.requireNonNull(data.getNonDefaultValues()));
  }

  private ClientboundTeleportEntityPacket createMovePacket() {
    return new ClientboundTeleportEntityPacket(this.nmsArmorStandBackbone);
  }

  private ClientboundRemoveEntitiesPacket createRemovePacket() {
    return new ClientboundRemoveEntitiesPacket(IntList.of(this.nmsArmorStandBackbone.getId()));
  }

}
