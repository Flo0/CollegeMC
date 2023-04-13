package net.collegemc.mc.libs.nametag;

import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.protocol.AbstractPacketInjector;
import net.collegemc.mc.libs.protocol.PacketHandler;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.bukkit.entity.Player;

public class NameTagSpawnPacketHandler extends PacketHandler<Player, ClientboundAddEntityPacket> {
  protected NameTagSpawnPacketHandler() {
    super(ClientboundAddEntityPacket.class, AbstractPacketInjector.Direction.OUTGOING);
  }

  @Override
  public void accept(Player player, ClientboundAddEntityPacket packet) {
    NameTagManager manager = CollegeLibrary.getNameTagManager();
    int entityId = packet.getId();
    NameTag nameTag = manager.getTag(entityId);
    if (nameTag != null) {
      nameTag.showTo(player);
    }
  }

  @Override
  protected boolean isAsync() {
    return true;
  }
}
