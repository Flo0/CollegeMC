package net.collegemc.mc.core.functionality.transport.teleport;

import lombok.RequiredArgsConstructor;
import net.collegemc.mc.core.functionality.active.ActiveCollegeUserManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class TeleportManager {

  private final ActiveCollegeUserManager activeCollegeUserManager;

  public CompletableFuture<Void> teleport(Player player, Location location) {
    return player.teleportAsync(location).thenApply(bool -> null);
  }

}
