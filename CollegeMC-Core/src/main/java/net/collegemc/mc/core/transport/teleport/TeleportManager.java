package net.collegemc.mc.core.transport.teleport;

import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class TeleportManager {

  public CompletableFuture<Void> teleport(Player player, Location location) {
    return player.teleportAsync(location).thenApply(bool -> null);
  }

}
