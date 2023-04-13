package net.collegemc.mc.libs.nametag;

import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.protocol.AbstractPacketInjector;
import net.collegemc.mc.libs.protocol.PacketHandler;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import org.bukkit.entity.Player;

public class NameTagPlayerPacketHandler extends PacketHandler<Player, ClientboundAddPlayerPacket> {
  protected NameTagPlayerPacketHandler() {
    super(ClientboundAddPlayerPacket.class, AbstractPacketInjector.Direction.OUTGOING);
  }

  @Override
  public void accept(Player player, ClientboundAddPlayerPacket clientboundAddPlayerPacket) {
    NameTagManager manager = CollegeLibrary.getNameTagManager();
    int entityId = clientboundAddPlayerPacket.getEntityId();
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
