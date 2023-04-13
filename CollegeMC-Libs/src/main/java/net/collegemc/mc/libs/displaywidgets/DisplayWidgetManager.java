package net.collegemc.mc.libs.displaywidgets;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.collegemc.mc.libs.displaywidgets.events.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DisplayWidgetManager {

  private static final int MAX_TRACE_DISTANCE = 12;

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

  public void createWindow(WidgetFrame frame, World world) {
    widgets.put(frame.getId(), frame);
    frame.build(world);
  }

  protected void onSwing(PlayerArmSwingEvent event) {
    if (!engagedPlayers.containsKey(event.getPlayer())) {
      return;
    }
    Set<WidgetFrame> engagedWidgets = engagedPlayers.get(event.getPlayer());
    Location eyeLocation = event.getPlayer().getEyeLocation();
    Vector eyeDirection = eyeLocation.getDirection();
    for (WidgetFrame widget : engagedWidgets) {
      Optional<Vector> hit = getBillboardRaytrace(eyeLocation, eyeDirection, widget);
      if (hit.isEmpty()) {
        continue;
      }
      Vector hitPosition = hit.get();
      Vector widgetPosition = widget.getDisplayEntity().getLocation().toVector();
      Vector relativeHitPoint = hitPosition.subtract(widgetPosition);
      relativeHitPoint.rotateAroundAxis(new Vector(0, 1.0, 0), -widget.getYaw());

      Vec2f inFramePosition = new Vec2f((float) Math.abs(relativeHitPoint.getX()) * 4, -(float) Math.abs(relativeHitPoint.getY()) * -4);
      ClickEvent clickEvent = new ClickEvent(event.getPlayer(), inFramePosition);
      widget.onClick(clickEvent);
    }
  }

  public Optional<Vector> getBillboardRaytrace(Location viewLocation, Vector view, WidgetFrame targetFrame) {
    Vector intersect = lineIntersection(targetFrame.getWorldPosition(), targetFrame.getRotation().clone().normalize(), viewLocation.toVector(), view);
    Vector topLeft = targetFrame.getTopLeft();
    Vector bottomRight = targetFrame.getBottomRight();
    if (!onPlane(intersect, topLeft, bottomRight)) {
      return Optional.empty();
    }
    if (intersect.toLocation(viewLocation.getWorld()).distance(viewLocation) >= MAX_TRACE_DISTANCE) {
      return Optional.empty();
    }
    return Optional.of(intersect);
  }

  public Vector lineIntersection(Vector planePoint, Vector planeNormal, Vector linePoint, Vector lineDirection) {
    if (planeNormal.dot(lineDirection.normalize()) == 0) {
      return null;
    }

    double t = (planeNormal.dot(planePoint) - planeNormal.dot(linePoint)) / planeNormal.dot(lineDirection.normalize());
    return linePoint.add(lineDirection.normalize().multiply(t));
  }

  private boolean onPlane(Vector position, Vector topLeft, Vector bottomRight) {
    return (position.getX() >= topLeft.getX() && position.getX() <= bottomRight.getX() || position.getX() <= topLeft.getX() && position.getX() >= bottomRight.getX())
            && ((position.getY() >= topLeft.getY() && position.getY() <= bottomRight.getY() || position.getY() <= topLeft.getY() && position.getY() >= bottomRight.getY()))
            && ((position.getZ() >= topLeft.getZ() && position.getZ() <= bottomRight.getZ() || position.getZ() <= topLeft.getZ() && position.getZ() >= bottomRight.getZ()));

  }

  public static String printVec2(Vec2f vec2) {
    return "x: " + vec2.x + " | y: " + vec2.y;
  }

}