package net.collegemc.mc.libs.nametag;

import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.protocol.AbstractPacketInjector;
import net.collegemc.mc.libs.protocol.PacketHandler;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import org.bukkit.entity.Player;

public class NameTagDespawnPacketHandler extends PacketHandler<Player, ClientboundRemoveEntitiesPacket> {
  protected NameTagDespawnPacketHandler() {
    super(ClientboundRemoveEntitiesPacket.class, AbstractPacketInjector.Direction.OUTGOING);
  }

  @Override
  public void accept(Player player, ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket) {
    NameTagManager manager = CollegeLibrary.getNameTagManager();
    for (int entityId : clientboundRemoveEntitiesPacket.getEntityIds()) {
      NameTag nameTag = manager.getTag(entityId);
      if (nameTag != null) {
        nameTag.hideFrom(player);
      }
    }
  }

  @Override
  protected boolean isAsync() {
    return true;
  }
}
