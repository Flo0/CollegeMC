package net.collegemc.mc.libs.displaywidgets;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.displaywidgets.events.ClickEvent;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class DisplayWidgetManager {

  private static final int MAX_TRACE_DISTANCE = 46;

  private final Map<Integer, WidgetFrame> widgets;
  private final Map<Player, Set<WidgetFrame>> engagedPlayers;
  private final Set<WidgetFrame> engagedWidgets;

  public DisplayWidgetManager(JavaPlugin plugin) {
    this.widgets = new HashMap<>();
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
    widget.build(world);
  }

  protected void onSwing(PlayerArmSwingEvent event) {
    if (!engagedPlayers.containsKey(event.getPlayer())) {
      return;
    }
    Logger logger = CollegeLibrary.getPlugin(CollegeLibrary.class).getLogger();
    Set<WidgetFrame> engagedWidgets = engagedPlayers.get(event.getPlayer());
    RayTraceResult result = event.getPlayer().rayTraceEntities(MAX_TRACE_DISTANCE);
    logger.info("result: " + result);
    if (result == null) {
      return;
    }
    for (WidgetFrame widget : engagedWidgets) {
      if (widget.getInteractionEntity().equals(result.getHitEntity())) {
        Vector hitPoint = result.getHitPosition();
        Vector widgetPosition = widget.getInteractionEntity().getLocation().toVector();
        Vector relativeHitPoint = hitPoint.subtract(widgetPosition);
        logger.warning("relativeHitPoint: " + relativeHitPoint);
        Vec2 inFramePosition = new Vec2((float) relativeHitPoint.getX(), (float) relativeHitPoint.getY());
        ClickEvent clickEvent = new ClickEvent(event.getPlayer(), inFramePosition);
        widget.onClick(clickEvent);
      }
    }
  }

}