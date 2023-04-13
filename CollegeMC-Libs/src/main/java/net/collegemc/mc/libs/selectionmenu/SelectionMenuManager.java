package net.collegemc.mc.libs.selectionmenu;

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
  }

  public boolean isInSelection(Player player) {
    return this.menuMap.containsKey(player);
  }

  public CompletableFuture<Void> startSelection(Player player, SelectionMenu selectionMenu) {
    if (this.isInSelection(player)) {
      this.endSelection(player, true);
    }
    this.menuMap.put(player, selectionMenu);
    return selectionMenu.onStart().thenRun(() -> selectionMenu.getTieDown().tieDown(player));
  }

  protected CompletableFuture<Void> endSelection(Player player, boolean now) {
    SelectionMenu menu = this.menuMap.remove(player);
    if (menu == null) {
      return CompletableFuture.completedFuture(null);
    }
    menu.getTieDown().release(player);
    return menu.onEnd(now);
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
