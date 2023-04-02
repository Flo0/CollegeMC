package net.collegemc.mc.libs.protocol;

import lombok.experimental.UtilityClass;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@UtilityClass
public class ProtocolManager {

  private static final BukkitPacketInjector packetInjector = new BukkitPacketInjector();

  public static void inject(Player player) {
    packetInjector.inject(player);
  }

  public static <T extends Packet<?>> void registerPacketHandler(PacketHandler<T> packetHandler) {
    packetInjector.register(packetHandler, packetHandler.getPacketType(), packetHandler.getDirections());
  }

  public static <T extends Packet<?>> void registerIncomingPacketHandler(Consumer<T> consumer, Class<T> type) {
    packetInjector.register(consumer, type, AbstractPacketInjector.Direction.INCOMING);
  }

  public static <T extends Packet<?>> void registerOutgoingPacketHandler(Consumer<T> consumer, Class<T> type) {
    packetInjector.register(consumer, type, AbstractPacketInjector.Direction.OUTGOING);
  }

  public static <T extends Packet<?>> void broadcastPacket(T packet) {
    MinecraftServer.getServer().getPlayerList().broadcastAll(packet);
  }

  public static <T extends Packet<?>> void sendTo(Player player, T packet) {
    ((CraftPlayer) player).getHandle().connection.send(packet);
  }

}
