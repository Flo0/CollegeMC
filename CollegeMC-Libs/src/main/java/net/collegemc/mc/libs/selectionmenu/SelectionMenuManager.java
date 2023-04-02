package net.collegemc.mc.libs.selectionmenu;

import net.collegemc.mc.libs.protocol.ProtocolManager;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SelectionMenuManager {

  private final Map<Player, SelectionMenu> menuMap;

  public SelectionMenuManager(JavaPlugin plugin) {
    this.menuMap = new HashMap<>();
    Bukkit.getPluginManager().registerEvents(new SelectionMenuListener(), plugin);
    TaskManager.runTaskTimer(this::tickMenus, 1, 1);
    ProtocolManager.registerPacketHandler(new SelectionMenuPacketListener());
  }

  public boolean isInSelection(Player player) {
    return this.menuMap.containsKey(player);
  }

  public CompletableFuture<Void> startSelection(Player player, SelectionMenu selectionMenu) {
    if (this.isInSelection(player)) {
      this.endSelection(player);
    }
    this.menuMap.put(player, selectionMenu);
    return selectionMenu.preStart().thenRun(() -> {
      TaskManager.runTask(() -> player.teleportAsync(selectionMenu.getSelectionLocation())
              .thenRun(() -> selectionMenu.getTieDown().tieDown(player, selectionMenu.getSelectionLocation())));
    });
  }

  protected CompletableFuture<Void> endSelection(Player player, boolean now) {
    SelectionMenu menu = this.menuMap.remove(player);
    if (menu == null) {
      return CompletableFuture.completedFuture(null);
    }
    menu.getTieDown().release(player);
    if (now) {
      menu.preEnd();
      if (menu.isTeleportOnEnd()) {
        player.teleport(menu.getReturnLocation());
      }
      return CompletableFuture.completedFuture(null);
    } else {
      return menu.preEnd().thenRun(() -> TaskManager.runTask(() -> {
        if (menu.isTeleportOnEnd()) {
          player.teleportAsync(menu.getReturnLocation());
        }
      }));
    }
  }

  public CompletableFuture<Void> endSelection(Player player) {
    return this.endSelection(player, false);
  }

  protected void swingHandle(Player player) {
    Optional.ofNullable(this.menuMap.get(player)).ifPresent(SelectionMenu::swingSelect);
  }

  private void tickMenus() {
    this.menuMap.values().forEach(menu -> {
      if (menu.isTicked()) {
        menu.onTick();
      }
    });
  }

}
