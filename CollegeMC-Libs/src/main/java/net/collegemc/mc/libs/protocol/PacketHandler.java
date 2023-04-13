package net.collegemc.mc.libs.protocol;

import lombok.Getter;
import net.minecraft.network.protocol.Packet;

import java.util.function.BiConsumer;

@Getter
public abstract class PacketHandler<T, P extends Packet<?>> implements BiConsumer<T, P> {

  private final Class<P> packetType;
  private final AbstractPacketInjector.Direction[] directions;

  protected PacketHandler(Class<P> packetType, AbstractPacketInjector.Direction direction) {
    this.packetType = packetType;
    this.directions = new AbstractPacketInjector.Direction[]{direction};
  }

  protected boolean isAsync() {
    return false;
  }

}
