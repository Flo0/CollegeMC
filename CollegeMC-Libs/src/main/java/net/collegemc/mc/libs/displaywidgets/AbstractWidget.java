package net.collegemc.mc.libs.displaywidgets;

import lombok.Getter;
import lombok.Setter;
import net.collegemc.mc.libs.displaywidgets.events.ClickEvent;
import net.collegemc.mc.libs.displaywidgets.events.HoverEnterEvent;
import net.collegemc.mc.libs.displaywidgets.events.HoverExitEvent;
import net.collegemc.mc.libs.displaywidgets.events.WidgetEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract sealed class AbstractWidget permits WidgetFrame, WidgetText, WidgetText.WidgetBackground {


  protected final List<AbstractWidget> children;
  @Getter
  protected final Vec2f position;
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
  public final static float CHILD_OFFSET = 0.0001f;

  protected AbstractWidget(Vec2f position, int width, int height, Color backgroundColor, double opacity) {
    this.position = position;
    this.width = width;
    this.height = height;
    this.backgroundColor = backgroundColor;
    this.opacity = opacity;
    this.children = new ArrayList<>();
    this.eventHandlers = new HashMap<>();
  }

  protected AbstractWidget(Vec2f position, int width, int height, Color backgroundColor) {
    this(position, width, height, backgroundColor, 0.5);
  }

  protected AbstractWidget(Vec2f position, int width, int height) {
    this(position, width, height, Color.GRAY, 0.5);
  }

  public void addChild(AbstractWidget child) {
    children.add(child);
  }

  public void spawn(World world, Vector spawnPosition) {
    Location spawnLocation = spawnPosition.toLocation(world);
    displayEntity = world.spawn(spawnLocation, TextDisplay.class, this::syncPropertiesWithWidget);
    children.forEach(child -> child.spawn(world, spawnPosition));
  }

  public void applyTransformation(Vector worldPosition, Vector facing, boolean passThrough, int depth) {
    Transformation currentTransformation = displayEntity.getTransformation();
    float yaw = getHalfYaw(facing);
    Quaternionf rotation = new Quaternionf().fromAxisAngleRad(new Vector3f(0.0F, 1.0F, 0.0F), yaw).normalize();
    Vector3f translation = currentTransformation.getTranslation();

    Vector translationHelp = facing.clone();
    translationHelp.rotateAroundY(Math.toRadians(-90d));

    // Entity offset
    Vector translationVector = translationHelp.clone();
    float verticalTranslation = (float) getHeight() / 4;
    translationVector.multiply((((float) getWidth() / 8) + ((float) getPosition().x / 4)));
    translationVector.add(new Vector(0, verticalTranslation, 0));

    //2d pos offset
    float verticalScale = (float) ((translationHelp.angle(new Vector(0, 1.0, 0)) / Math.toRadians(90d)));
    float verticalPosOffset = (verticalScale / 4) * getPosition().y;
    translationVector.add(new Vector(0.0d, verticalPosOffset, 0.0d));
    Vector resultingRelativePos = displayEntity.getLocation().toVector().add(translationVector);
    float xOffset = ((float) worldPosition.getX() - (float) resultingRelativePos.getX());
    float yOffset = ((float) worldPosition.getY() - (float) resultingRelativePos.getY());
    float zOffset = ((float) worldPosition.getZ() - (float) resultingRelativePos.getZ());

    translation.set(new Vector3f(xOffset, yOffset, zOffset));
    Transformation newTransformation = new Transformation(translation, rotation, currentTransformation.getScale(), rotation.invert());
    displayEntity.setTransformation(newTransformation);
    final Vector childFacing = facing.clone();
    final int childDepth = depth + 1;

    if (passThrough) {
      children.forEach(child -> child.applyTransformation(worldPosition.add(facing.multiply(CHILD_OFFSET).multiply(childDepth)), childFacing, true, childDepth));
    }

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
    entity.setAlignment(TextDisplay.TextAlignment.CENTER);
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

  public boolean contains(Vec2f pos2D) {
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
      Vec2f childClickPos = event.getPosition().clone().add(this.position.clone().negated());
      ClickEvent childEvent = new ClickEvent(event.getActor(), childClickPos);
      List<AbstractWidget> hit = children.stream().filter(child -> child.contains(childClickPos)).toList();
      children.stream().filter(child -> child.contains(childClickPos)).forEach(child -> child.onClick(childEvent));
    }
  }

  protected float getHalfYaw(Vector facing) {
    Vector axis = new Vector(1.0, 0.0, 0.0);
    float yaw = facing.angle(axis) - (float) (Math.PI / 2.0);
    if (facing.getZ() < 0) {
      yaw = (float) (yaw + Math.toRadians(180d));
    }
    yaw = yaw / 2;

    if (yaw < 0) {
      yaw = (float) Math.toRadians(360d) + yaw;
    }
    return yaw;
  }

  @Override
  public String toString() {
    return "AbstractWidget{" +
            "children=" + children.size() +
            ", position=" + position +
            ", eventHandlers=" + eventHandlers +
            ", width=" + width +
            ", height=" + height +
            ", backgroundColor=" + backgroundColor +
            '}';
  }
}
