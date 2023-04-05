package net.collegemc.mc.libs.displaywidgets;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.collegemc.mc.libs.tasks.MongoBackedMap;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

import java.io.Flushable;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DisplayWidgetManager implements Flushable {

  public static final String NAMESPACE = "Display-Widgets";
  private static final double MAX_TRACE_DISTANCE = 64.0D;

  private final MongoBackedMap<Integer, WidgetFrame> widgets;
  private final Map<Player, Set<WidgetFrame>> engagedPlayers;
  private final Set<WidgetFrame> engagedWidgets;

  public DisplayWidgetManager(JavaPlugin plugin) {
    this.widgets = new MongoBackedMap<>(new HashMap<>(), NAMESPACE, Integer.class, WidgetFrame.class);
    this.engagedPlayers = new HashMap<>();
    this.engagedWidgets = new HashSet<>();
    Bukkit.getPluginManager().registerEvents(new WidgetListener(), plugin);
  }

  public boolean engage(Player player, int widgetId) {
    WidgetFrame widget = widgets.get(widgetId);
    if (widget == null) {
      return false;
    }
    if (engagedWidgets.contains(widget)) {
      return false;
    }
    engagedWidgets.add(widget);
    engagedPlayers.computeIfAbsent(player, key -> new HashSet<>()).add(widget);
    return true;
  }

  public void disengage(Player player, int widgetId) {
    WidgetFrame widget = widgets.get(widgetId);
    if (widget == null) {
      return;
    }
    engagedWidgets.remove(widget);
    Set<WidgetFrame> frames = engagedPlayers.computeIfAbsent(player, key -> new HashSet<>());
    frames.remove(widget);
    if (frames.isEmpty()) {
      engagedPlayers.remove(player);
    }
  }

  public void createWindow(WidgetFrame widget, World world) {
    widgets.put(widget.getId(), widget);
    widget.spawn(world, widget.getWorldPosition(), widget.getRotation());
  }

  protected void onSwing(PlayerArmSwingEvent event) {
    if (!engagedPlayers.containsKey(event.getPlayer())) {
      return;
    }
    Map<Player, Set<WidgetFrame>> activeWidgets = new HashMap<>(engagedPlayers);
    RayTraceResult result = event.getPlayer().rayTraceEntities(64);
    if (result == null) {
    }

  }

  public void load() {
    widgets.loadDataFromRemote();
  }

  @Override
  public void flush() throws IOException {
    widgets.saveDataToRemote();
  }

}