package net.collegemc.mc.libs.displaywidgets;

import lombok.Getter;
import lombok.Setter;
import net.collegemc.mc.libs.CollegeLibrary;
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
import java.util.logging.Logger;

public abstract sealed class AbstractWidget permits WidgetText, WidgetFrame {

  @Getter
  protected final int id;
  protected final List<AbstractWidget> children;
  @Getter
  protected final Vec2 position;
  protected final Map<Class<? extends WidgetEvent>, List<Consumer<? extends WidgetEvent>>> eventHandlers;
  @Getter
  @Setter
  private int width;
  @Getter
  @Setter
  private int height;
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
  private TextDisplay displayEntity;
  @Getter
  @Setter
  private Color glowColor = Color.WHITE;
  @Getter
  @Setter
  private boolean glowColorEnabled = false;
  protected Vector worldTo2D;

  protected AbstractWidget(int id, Vec2 position, int width, int height, Color backgroundColor, double opacity) {
    this.id = id;
    this.position = position;
    this.width = width;
    this.height = height;
    this.backgroundColor = backgroundColor;
    this.opacity = opacity;
    this.children = new ArrayList<>();
    this.eventHandlers = new HashMap<>();
    worldTo2D = new Vector((double) (this.getWidth() / 2) / 4, (double) (-this.getHeight()) / 4, 0.0);
  }

  protected AbstractWidget(int id, Vec2 position, int width, int height, Color backgroundColor) {
    this(id, position, width, height, backgroundColor, 0.5);
  }

  protected AbstractWidget(int id, Vec2 position, int width, int height) {
    this(id, position, width, height, Color.GRAY, 0.5);
  }

  public void addChild(AbstractWidget child) {
    children.add(child);
  }

  public void spawn(World world, Vector spawnPosition, Vector spawnRotation) {
    //Get Logger and print relevant positions
    Logger logger = CollegeLibrary.getPlugin(CollegeLibrary.class).getLogger();
    logger.warning("Now spawning id: " + this.id);
    logger.info("worldPosition: " + spawnPosition);
    // Vector up = new Vector(0.0, 1.0, 0.0);
    Vector transformationVector = new Vector(this.position.x, this.position.y, 0.0);
    logger.info("transformationVector: " + transformationVector);
    transformationVector.add(worldTo2D);
    logger.info("transformationVector with positionTransform: " + transformationVector);
    // float yaw = spawnRotation.angle(up) - (float) (Math.PI / 2.0);
    Location spawnLocation = spawnPosition.toLocation(world).add(transformationVector);
    logger.info("final spawnPosition: " + spawnLocation);
    spawnLocation.setDirection(spawnRotation);
    displayEntity = world.spawn(spawnLocation, TextDisplay.class, this::syncPropertiesWithWidget);
    Vector childRotation = spawnRotation.clone();
    logger.info("(child) Rotation: " + childRotation);
    Vector childPosition = spawnPosition.add(new Vector(0, 0, childRotation.getZ() * (-0.001)));
    logger.info("childPosition: " + childPosition);
    children.forEach(child -> child.spawn(world, childPosition, childRotation));
  }

  public void update() {
    syncPropertiesWithWidget(this.displayEntity);
  }


  @SuppressWarnings("deprecation")
  private void syncPropertiesWithWidget(TextDisplay entity) {
    entity.setLineWidth(width * 10);
    String filler = "ꈰ".repeat(width * height);
    entity.text(Component.text(filler));
    entity.setTextOpacity((byte) -1);
    entity.setBackgroundColor(this.backgroundColor);
    entity.setShadowed(this.shadowed);
    entity.setShadowRadius(this.shadowRadius);
    entity.setShadowStrength(this.shadowStrength);
    entity.setAlignment(TextDisplay.TextAligment.CENTER);
    entity.setSeeThrough(false);
    entity.setBillboard(Display.Billboard.FIXED);
    if (this.glowColorEnabled) {
      entity.setGlowing(true);
      entity.setGlowColorOverride(this.glowColor);
    } else {
      entity.setGlowing(false);
    }
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
