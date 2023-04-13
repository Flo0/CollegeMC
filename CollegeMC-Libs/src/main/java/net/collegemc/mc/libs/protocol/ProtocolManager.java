package net.collegemc.mc.libs.protocol;

import lombok.experimental.UtilityClass;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

@UtilityClass
public class ProtocolManager {

  private static final BukkitPacketInjector packetInjector = new BukkitPacketInjector();

  static void inject(Player player) {
    packetInjector.inject(player);
  }

  static void uninject(Player player) {
    packetInjector.uninject(player);
  }

  public static <T extends Packet<?>> void registerPacketHandler(PacketHandler<Player, T> packetHandler) {
    packetInjector.register(packetHandler);
  }

  public static <T extends Packet<?>> void broadcastPacket(T packet) {
    MinecraftServer.getServer().getPlayerList().broadcastAll(packet);
  }

  public static <T extends Packet<?>> void sendTo(Player player, T packet) {
    ((CraftPlayer) player).getHandle().connection.send(packet);
  }

}
