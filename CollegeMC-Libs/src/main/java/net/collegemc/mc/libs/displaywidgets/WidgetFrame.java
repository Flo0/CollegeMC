package net.collegemc.mc.libs.displaywidgets;

import lombok.Getter;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Vector;

public final class WidgetFrame extends AbstractWidget {

  @Getter
  private final Vector worldPosition;
  @Getter
  private final Vector rotation;
  @Getter
  private transient Interaction interactionEntity;

  public WidgetFrame(int id, Vector worldPosition, Vector rotation, float width, float height, Color backgroundColor, double opacity) {
    super(id, new Vec2(0, 0), width, height, backgroundColor, opacity);
    this.worldPosition = worldPosition;
    this.rotation = rotation;
  }

  public WidgetFrame(int id, Vector worldPosition, Vector rotation, float width, float height, Color backgroundColor) {
    this(id, worldPosition, rotation, width, height, backgroundColor, 1.0);
  }

  public WidgetFrame(int id, Vector worldPosition, Vector direction, float width, float height) {
    this(id, worldPosition, direction, width, height, Color.WHITE, 1.0);
  }

  public void build(World world) {
    super.spawn(world, worldPosition, rotation);
    createInteractionEntity(world);
  }

  @Override
  public void destroy() {
    super.destroy();
    interactionEntity.remove();
  }

  @Override
  public void update() {
    super.update();
  }

  private void createInteractionEntity(World world) {
    Location spawnLocation = this.worldPosition.toLocation(world);
    spawnLocation.setDirection(this.rotation);
    this.interactionEntity = world.spawn(spawnLocation, Interaction.class, entity -> {
      entity.setInteractionHeight(this.getHeight());
      entity.setInteractionWidth(this.getWidth());
    });
  }

}
