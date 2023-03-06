package net.collegemc.mc.libs.resourcepack.distribution;

import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.tasks.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent.Status;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ResourcePackListener implements Listener {

  public ResourcePackListener(final ResourcepackManager manager) {
    this.manager = manager;
  }

  private final ResourcepackManager manager;
  private final Set<UUID> attempts = new HashSet<>();

  @EventHandler(priority = EventPriority.HIGH)
  public void onJoin(final PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    TaskManager.runTaskLater(() -> this.sendResourcePack(player), 20L);
  }

  @EventHandler
  public void resourceStatusEvent(final PlayerResourcePackStatusEvent event) {
    final Player player = event.getPlayer();
    final Status status = event.getStatus();
    if (status == Status.SUCCESSFULLY_LOADED) {
      Msg.sendInfo(player, "Das Resourcepack wurde akzeptiert.");
    } else if (status == Status.FAILED_DOWNLOAD) {
      UUID id = player.getUniqueId();
      if (this.attempts.contains(id)) {
        this.attempts.remove(id);
        player.kick(Component.text("Bitte akzeptiere das Resourcepack."));
      } else {
        this.attempts.add(id);
        TaskManager.runTaskLater(() -> this.sendResourcePack(player), 60L);
      }
    } else if (status == Status.DECLINED) {
      player.kick(Component.text("Bitte akzeptiere das Resourcepack."));
    }
  }

  private void sendResourcePack(final Player player) {
    Bukkit.getLogger().info("Setting resourcepack for: " + player.getName());
    TextComponent component = Component.text("§fBitte benutze das resourcepack.\n§fEs überschreibt keine deiner Texturen.");
    player.setResourcePack(this.manager.getDownloadURL(), this.manager.getResourceHash(), true, component);
  }

}