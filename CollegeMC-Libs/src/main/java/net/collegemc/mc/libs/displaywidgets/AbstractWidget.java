package net.collegemc.mc.libs.displaywidgets;

import lombok.Getter;
import lombok.Setter;
import net.collegemc.mc.libs.displaywidgets.events.ClickEvent;
import net.collegemc.mc.libs.displaywidgets.events.HoverEnterEvent;
import net.collegemc.mc.libs.displaywidgets.events.HoverExitEvent;
import net.collegemc.mc.libs.displaywidgets.events.WidgetEvent;
import net.kyori.adventure.text.Component;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract sealed class AbstractWidget permits WidgetButton, WidgetFrame {

  @Getter
  protected final int id;
  protected final List<AbstractWidget> children;
  @Getter
  protected final Vec2 position;
  protected final Map<Class<? extends WidgetEvent>, List<Consumer<? extends WidgetEvent>>> eventHandlers;
  @Getter
  @Setter
  private float width;
  @Getter
  @Setter
  private float height;
  @Getter
  @Setter
  private Color backgroundColor;
  @Getter
  @Setter
  private double opacity;
  @Getter
  @Setter
  private boolean shadowed = false;
  @Getter
  @Setter
  private float shadowRadius = 0.1F;
  @Getter
  @Setter
  private float shadowStrength = 0.5F;
  @Getter
  @Setter
  private TextDisplay.TextAligment alignment = TextDisplay.TextAligment.LEFT;
  @Getter
  @Setter
  private boolean seeThrough = false;
  @Getter
  @Setter
  private TextDisplay displayEntity;
  @Getter
  @Setter
  private int lineWidth = 128;
  @Getter
  @Setter
  private Color glowColor = Color.WHITE;
  @Getter
  @Setter
  private boolean glowColorEnabled = false;
  @Getter
  @Setter
  private Component text = Component.empty();

  protected AbstractWidget(int id, Vec2 position, float width, float height, Color backgroundColor, double opacity) {
    this.id = id;
    this.position = position;
    this.width = width;
    this.height = height;
    this.backgroundColor = backgroundColor;
    this.opacity = opacity;
    this.children = new ArrayList<>();
    this.eventHandlers = new HashMap<>();
  }

  protected AbstractWidget(int id, Vec2 position, float width, float height, Color backgroundColor) {
    this(id, position, width, height, backgroundColor, 0.5);
  }

  protected AbstractWidget(int id, Vec2 position, float width, float height) {
    this(id, position, width, height, Color.GRAY, 0.5);
  }

  public void addChild(AbstractWidget child) {
    children.add(child);
  }

  public void spawn(World world, Vector spawnPosition, Vector spawnRotation) {
    Vector up = new Vector(0.0, 1.0, 0.0);
    Vector transformationVector = new Vector(this.position.x, this.position.y, 0.0);
    float yaw = spawnRotation.angle(up) - (float) (Math.PI / 2.0);
    transformationVector.rotateAroundAxis(up, yaw);
    Location spawnLocation = spawnPosition.toLocation(world).add(transformationVector);
    spawnLocation.setDirection(spawnRotation);
    displayEntity = world.spawn(spawnLocation, TextDisplay.class, this::syncPropertiesWithWidget);
    Vector childRotation = spawnRotation.clone();
    Vector childPosition = spawnLocation.toVector().add(childRotation.clone().multiply(0.01));
    children.forEach(child -> child.spawn(world, childPosition, childRotation));
  }

  public void update() {
    syncPropertiesWithWidget(this.displayEntity);
  }

  @SuppressWarnings("deprecation")
  private void syncPropertiesWithWidget(TextDisplay entity) {
    entity.setDisplayWidth(this.width);
    entity.setDisplayHeight(this.height);
    entity.setBackgroundColor(this.backgroundColor);
    entity.setShadowed(this.shadowed);
    entity.setShadowRadius(this.shadowRadius);
    entity.setShadowStrength(this.shadowStrength);
    entity.setAlignment(this.alignment);
    entity.setLineWidth(this.lineWidth);
    entity.setSeeThrough(this.seeThrough);
    entity.setBillboard(Display.Billboard.FIXED);
    if (this.glowColorEnabled) {
      entity.setGlowing(true);
      entity.setGlowColorOverride(this.glowColor);
    } else {
      entity.setGlowing(false);
    }
    entity.text(this.text);
  }

  public void destroy() {
    this.displayEntity.remove();
    this.children.forEach(AbstractWidget::destroy);
  }

  public <T extends WidgetEvent> void addEventHandler(Class<T> eventClass, Consumer<T> handler) {
    this.eventHandlers.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(handler);
  }

  public boolean contains(Vec2 pos2D) {
    return pos2D.x >= this.position.x && pos2D.x <= this.position.x + this.width && pos2D.y >= this.position.y && pos2D.y <= this.position.y + this.height;
  }

  @SuppressWarnings("unchecked")
  public void onHoverEnter(HoverEnterEvent event) {
    this.eventHandlers.getOrDefault(HoverEnterEvent.class, List.of()).forEach(handler -> ((Consumer<HoverEnterEvent>) handler).accept(event));
  }

  @SuppressWarnings("unchecked")
  public void onHoverExit(HoverExitEvent event) {
    this.eventHandlers.getOrDefault(HoverExitEvent.class, List.of()).forEach(handler -> ((Consumer<HoverExitEvent>) handler).accept(event));
  }

  @SuppressWarnings("unchecked")
  public void onClick(ClickEvent event) {
    if (children.isEmpty()) {
      this.eventHandlers.getOrDefault(ClickEvent.class, List.of()).forEach(handler -> ((Consumer<ClickEvent>) handler).accept(event));
    } else {
      Vec2 childClickPos = event.getPosition().add(this.position.negated());
      ClickEvent childEvent = new ClickEvent(event.getActor(), childClickPos);
      children.stream().filter(child -> child.contains(childClickPos)).forEach(child -> child.onClick(childEvent));
    }
  }

}
