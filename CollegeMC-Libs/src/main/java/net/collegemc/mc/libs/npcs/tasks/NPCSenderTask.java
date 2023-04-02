package net.collegemc.mc.libs.npcs.tasks;

import net.collegemc.mc.libs.npcs.abstraction.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.UUID;

public class NPCSenderTask extends BukkitRunnable {

  private final UUID playerId;
  private final Deque<NPC> npcQueue;

  public NPCSenderTask(UUID playerId, Collection<NPC> npcCollection) {
    System.out.printf("Sending %d NPCs.%n", npcCollection.size());
    this.playerId = playerId;
    this.npcQueue = new ArrayDeque<>(npcCollection);
  }


  @Override
  public void run() {
    Player player = Bukkit.getPlayer(this.playerId);
    if (player == null) {
      this.cancel();
      return;
    }

    NPC nextNPC = this.npcQueue.poll();
    if (nextNPC == null) {
      this.cancel();
      return;
    }

    nextNPC.showTo(player);
  }
}
