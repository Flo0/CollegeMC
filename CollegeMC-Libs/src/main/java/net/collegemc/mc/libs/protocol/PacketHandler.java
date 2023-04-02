package net.collegemc.mc.libs.protocol;

import lombok.Getter;
import net.minecraft.network.protocol.Packet;

import java.util.function.Consumer;

@Getter
public abstract class PacketHandler<T extends Packet<?>> implements Consumer<T> {

  private final Class<T> packetType;
  private final AbstractPacketInjector.Direction[] directions;

  protected PacketHandler(Class<T> packetType, AbstractPacketInjector.Direction... directions) {
    this.packetType = packetType;
    this.directions = directions;
  }
}
