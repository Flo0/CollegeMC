package net.collegemc.mc.libs.displaywidgets;

import lombok.Getter;
import net.collegemc.mc.libs.CollegeLibrary;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class WidgetFrame extends AbstractWidget {

  @Getter
  private final Vector worldPosition;
  @Getter
  private Vector rotation;


  public WidgetFrame(int id, Vector worldPosition, Vector rotation, int width, int height, Color backgroundColor, double opacity) {
    super(id, new Vec2(0, 0), width, height, backgroundColor, opacity);
    if (rotation.isZero()) {
      throw new IllegalArgumentException("Rotation cannot be zero!");
    }
    this.worldPosition = worldPosition;
    this.rotation = rotation.normalize().clone();
    CollegeLibrary.getInstance().getLogger().warning("rotation: " + rotation);
  }

  public WidgetFrame(int id, Vector worldPosition, Vector rotation, int width, int height, Color backgroundColor) {
    this(id, worldPosition, rotation, width, height, backgroundColor, 1.0);
  }

  public WidgetFrame(int id, Vector worldPosition, Vector direction, int width, int height) {
    this(id, worldPosition, direction, width, height, Color.WHITE, 1.0);
  }


  public void build(World world) {
    super.spawn(world, worldPosition.clone());
    applyTransformation(worldPosition, rotation.clone(), true);
    update();
  }

  @Override
  public void destroy() {
    super.destroy();
  }


  @Override
  public void update() {
    super.update();
  }

  @Override
  public void applyTransformation(Vector worldPosition, Vector facing, boolean passThrough) {
    super.applyTransformation(worldPosition.add(facing.clone().multiply(CHILD_OFFSET)), facing.clone(), passThrough);
    this.rotation = facing.clone();
    update();
  }

  public Vector getBottomRight() {
    Vector entityLocation = getDisplayEntity().getLocation().toVector().clone();
    Vector translationHelp = rotation.clone();
    translationHelp.rotateAroundY(Math.toRadians(-90d));
    translationHelp.multiply(-(float) getWidth() / 4);
    translationHelp.add(new Vector(0, -(getHeight() / 4), 0));

    return entityLocation.clone().add(translationHelp);
  }

  public Vector getTopLeft() {
    return worldPosition;
  }

  public float getYaw() {
    return getHalfYaw(rotation) * 2 % 365;
  }

}
