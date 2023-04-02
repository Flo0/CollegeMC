package net.collegemc.mc.libs.selectionmenu;

import net.collegemc.mc.libs.protocol.AbstractPacketInjector;
import net.collegemc.mc.libs.protocol.PacketHandler;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;

public class SelectionMenuPacketListener extends PacketHandler<ServerboundSwingPacket> {

  protected SelectionMenuPacketListener() {
    super(ServerboundSwingPacket.class, AbstractPacketInjector.Direction.INCOMING);
  }

  @Override
  public void accept(ServerboundSwingPacket serverboundSwingPacket) {
    System.out.println("SWING");
  }
}
