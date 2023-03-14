package net.collegemc.mc.libs.npcs.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.npcs.abstraction.NPC;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.plugin.Plugin;

public class NPCPacketListener extends PacketAdapter {
  public NPCPacketListener(Plugin plugin) {
    super(plugin, PacketType.Play.Client.USE_ENTITY);
  }

  @Override
  public void onPacketReceiving(PacketEvent event) {
    PacketContainer container = event.getPacket();
    Integer id = container.getIntegers().read(0);

    if (id == null) {
      return;
    }

    NPC npc = CollegeLibrary.getNpcManager().getByEntityId(id);

    if (npc == null) {
      return;
    }

    TaskManager.runTask(() -> npc.onClick(event.getPlayer()));
  }
}
