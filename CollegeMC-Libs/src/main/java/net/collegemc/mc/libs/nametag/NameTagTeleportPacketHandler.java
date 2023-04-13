package net.collegemc.mc.libs.nametag;

import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.protocol.AbstractPacketInjector;
import net.collegemc.mc.libs.protocol.PacketHandler;
import net.collegemc.mc.libs.tasks.TaskManager;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import org.bukkit.entity.Player;

public class NameTagTeleportPacketHandler extends PacketHandler<Player, ClientboundTeleportEntityPacket> {
  protected NameTagTeleportPacketHandler() {
    super(ClientboundTeleportEntityPacket.class, AbstractPacketInjector.Direction.OUTGOING);
  }

  @Override
  public void accept(Player player, ClientboundTeleportEntityPacket clientboundTeleportEntityPacket) {
    NameTagManager manager = CollegeLibrary.getNameTagManager();
    NameTag nameTag = manager.getTag(clientboundTeleportEntityPacket.getId());
    if (nameTag != null) {
      TaskManager.runTaskLaterAsync(() -> nameTag.remountFor(player), 1L);
    }
  }

  @Override
  protected boolean isAsync() {
    return true;
  }
}
