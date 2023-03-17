package net.collegemc.mc.libs.npcs;

import com.comphenix.protocol.ProtocolLibrary;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.collegemc.common.mongodb.MongoMap;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.npcs.abstraction.NPC;
import net.collegemc.mc.libs.npcs.listener.NPCListener;
import net.collegemc.mc.libs.npcs.listener.NPCPacketListener;
import net.collegemc.mc.libs.npcs.tasks.NPCSenderTask;
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
  private final Map<String, NPC> nameMap = new HashMap<>();
  private final Map<String, NPC> npcMongoMap;

  public NPCManager(JavaPlugin plugin) {
    MongoDatabase database = CollegeLibrary.getServerDatabase();
    MongoCollection<NPC> collection = database.getCollection(NAMESPACE, NPC.class);

    this.npcMongoMap = new MongoMap<>(collection, CollegeLibrary.getGsonSerializer(), String.class);

    this.loadAllNPCs();

    ProtocolLibrary.getProtocolManager().addPacketListener(new NPCPacketListener(plugin));
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

  private void loadAllNPCs() {
    this.npcMongoMap.values().forEach(npc -> this.add(npc, false));
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
    this.add(npc, true);
  }

  private void add(NPC npc, boolean remoteMirror) {
    this.activeMap.put(npc.getEntityId(), npc);
    this.nameMap.put(npc.getInternalName(), npc);
    if (remoteMirror) {
      TaskManager.runOnIOPool(() -> this.npcMongoMap.put(npc.getInternalName(), npc));
    }
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
      TaskManager.runOnIOPool(() -> this.npcMongoMap.remove(npc.getInternalName()));
    }
  }

  public void removeByName(String name) {
    NPC npc = this.nameMap.remove(name);
    if (npc != null) {
      this.activeMap.remove(npc.getEntityId());
      TaskManager.runOnIOPool(() -> this.npcMongoMap.remove(npc.getInternalName()));
    }
  }

  @Override
  public void flush() {
    this.npcMongoMap.putAll(this.nameMap);
  }
}
