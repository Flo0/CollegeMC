package net.collegemc.mc.libs.npcs.listener;

import net.collegemc.mc.libs.protocol.AbstractPacketInjector;
import net.collegemc.mc.libs.protocol.PacketHandler;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;

public class NPCPacketListener extends PacketHandler<ServerboundInteractPacket> {
  public NPCPacketListener() {
    super(ServerboundInteractPacket.class, AbstractPacketInjector.Direction.INCOMING);
  }

  @Override
  public void accept(ServerboundInteractPacket serverboundInteractPacket) {
    System.out.println(">> Packet received " + serverboundInteractPacket.getClass());
  }
}
