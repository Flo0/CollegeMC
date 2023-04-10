package net.collegemc.mc.libs.displaywidgets;

import lombok.Getter;
import lombok.Setter;
import net.collegemc.mc.libs.CollegeLibrary;
import net.kyori.adventure.text.Component;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;

import java.util.logging.Logger;

public non-sealed class WidgetText extends AbstractWidget {

  private TextDisplay textDisplay;
  @Getter
  @Setter
  private Vec2 textPosition;
  @Getter
  @Setter
  private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
  @Getter
  @Setter
  private Component text;
  @Getter
  @Setter
  private float textDisplayWidth = 1.0F;
  @Getter
  @Setter
  private float textDisplayHeight = 1.0F;
  @Getter
  @Setter
  private int charactersPerLine = 128;
  @Getter
  @Setter
  private TextDisplay.TextAligment alignment = TextDisplay.TextAligment.LEFT;

  public WidgetText(int id, Component text, Vec2 position, int width, int height, Color backgroundColor, double opacity) {
    super(++id, new Vec2(position.x, position.y), width, height, backgroundColor, opacity);
    this.text = text;
    this.textPosition = position;
  }

  public WidgetText(int id, Vec2 position, int width, int height, Color backgroundColor, double opacity) {
    this(id, null, position, width, height, backgroundColor, opacity);
  }

  @Override
  public void spawn(World world, Vector spawnPosition, Vector spawnRotation) {
    super.spawn(world, spawnPosition, spawnRotation);

    Logger logger = CollegeLibrary.getPlugin(CollegeLibrary.class).getLogger();
    logger.warning("Now spawning text id: " + (id - 1));

    // Vector up = new Vector(0.0, 1.0, 0.0);
    float textYPosition = switch (verticalAlignment) {
      case TOP -> this.textPosition.y / 4;
      case CENTER -> this.textPosition.y / 4 + (((float) this.getHeight() / 2) / 4) - (float) 1 / 8 * getLines(text.toString());
      case BOTTOM -> this.textPosition.y / 4 + (((float) this.getHeight()) / 4) - (float) 1 / 4 * getLines(text.toString());
    };
    logger.info("textYPosition: " + textYPosition);
    Vector transformationVector = new Vector(this.textPosition.x / 4, -textYPosition, 0.0);
    logger.info("transformation: " + transformationVector);
    Vector positionTransformation = new Vector((double) (this.getWidth() / 2) / 4, -((double) 1 / 4 * getLines(text.toString())), 0);
    logger.info("3D -> 2D transformation: " + positionTransformation);
    transformationVector.add(positionTransformation);

    // float yaw = spawnRotation.angle(up) - (float) (Math.PI / 2.0);
    // transformationVector.rotateAroundAxis(up, yaw);
    Location spawnLocation = spawnPosition.toLocation(world).add(transformationVector).add(new Vector(0, 0, spawnRotation.getZ() * -0.001));
    logger.info("final spawn location " + spawnLocation);
    spawnLocation.setDirection(spawnRotation);
    textDisplay = world.spawn(spawnLocation, TextDisplay.class, this::syncPropertiesWithText);
  }

  @Override
  public void update() {
    syncPropertiesWithText(textDisplay);
    super.update();
  }

  @Override
  public void destroy() {
    this.textDisplay.remove();
    super.destroy();
  }

  private int getLines(String text) {
    return (int) text.lines().count();
  }

  @SuppressWarnings("deprecation")
  private void syncPropertiesWithText(TextDisplay entity) {
    entity.text(this.text);
    entity.setShadowed(this.isShadowed());
    entity.setShadowRadius(this.getShadowRadius());
    entity.setShadowStrength(this.getShadowStrength());
    entity.setAlignment(this.alignment);
    entity.setLineWidth(this.charactersPerLine);
    entity.setSeeThrough(false);
    entity.setBillboard(Display.Billboard.FIXED);
    entity.setGlowing(false);
    entity.setBackgroundColor(Color.fromARGB(0, 255, 0, 0));
  }

  public enum VerticalAlignment {
    TOP,
    CENTER,
    BOTTOM
  }

}
