package net.collegemc.mc.libs.npcs;

import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.npcs.abstraction.NPC;
import net.collegemc.mc.libs.npcs.listener.NPCListener;
import net.collegemc.mc.libs.npcs.listener.NPCPacketListener;
import net.collegemc.mc.libs.npcs.tasks.NPCSenderTask;
import net.collegemc.mc.libs.protocol.ProtocolManager;
import net.collegemc.mc.libs.tasks.MongoBackedMap;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.Flushable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCManager implements Flushable {

  public static final String NAMESPACE = "NPCs";
  private static final int ticksPerNPCSend = 4;

  private final Map<Integer, NPC> activeMap = new HashMap<>();
  private final MongoBackedMap<String, NPC> nameMap;

  public NPCManager(JavaPlugin plugin) {
    this.nameMap = new MongoBackedMap<>(new HashMap<>(), NAMESPACE, String.class, NPC.class);
    this.nameMap.loadDataFromRemote();
    this.nameMap.values().forEach(npc -> this.activeMap.put(npc.getEntityId(), npc));

    ProtocolManager.registerPacketHandler(new NPCPacketListener());
    CollegeLibrary.getCommandManager().registerCommand(new NPCCommand());
    CollegeLibrary.getCommandManager().getCommandCompletions().registerCompletion("NPC", context -> {
      return List.copyOf(this.nameMap.keySet());
    });
    Bukkit.getPluginManager().registerEvents(new NPCListener(), plugin);
    TaskManager.runTaskTimer(this::tickNPCs, 1, 1);
  }

  public void sendAllNPCsAsync(Player player) {
    NPCSenderTask task = new NPCSenderTask(player.getUniqueId(), this.activeMap.values());
    TaskManager.runTaskTimerAsync(task, ticksPerNPCSend, ticksPerNPCSend);
  }

  private void tickNPCs() {
    for (NPC npc : this.activeMap.values()) {
      if (!npc.isTicked() || !npc.isInLoadedChunk()) {
        continue;
      }
      npc.onTick();
    }
  }

  public void add(NPC npc) {
    if (npc.isPersistent()) {
      this.nameMap.put(npc.getInternalName(), npc);
    } else {
      this.nameMap.putLocal(npc.getInternalName(), npc);
    }
    this.activeMap.put(npc.getEntityId(), npc);
  }

  public NPC getByEntityId(int id) {
    return this.activeMap.get(id);
  }

  public NPC getByName(String name) {
    return this.nameMap.get(name);
  }

  public void removeById(int id) {
    NPC npc = this.activeMap.remove(id);
    if (npc != null) {
      this.nameMap.remove(npc.getInternalName());
    }
  }

  public void removeByName(String name) {
    NPC npc = this.nameMap.remove(name);
    if (npc != null) {
      this.activeMap.remove(npc.getEntityId());
    }
  }

  @Override
  public void flush() {
    this.nameMap.saveDataToRemote();
  }
}
