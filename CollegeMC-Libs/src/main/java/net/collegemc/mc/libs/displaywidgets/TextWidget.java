package net.collegemc.mc.libs.displaywidgets;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;

public non-sealed class TextWidget extends AbstractWidget {

  private TextDisplay textDisplay;
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

  public TextWidget(int id, Component text, Vec2 position, int width, int height, Color backgroundColor, double opacity) {
    super(id, position, width, height, backgroundColor, opacity);
    this.text = text;
  }

  public TextWidget(int id, Vec2 position, int width, int height, Color backgroundColor, double opacity) {
    this(id, null, position, width, height, backgroundColor, opacity);
  }

  @Override
  public void spawn(World world, Vector spawnPosition, Vector spawnRotation) {
    Vector up = new Vector(0.0, 1.0, 0.0);
    Vector transformationVector = new Vector(this.position.x, this.position.y, 0.0);
    float yaw = spawnRotation.angle(up) - (float) (Math.PI / 2.0);
    transformationVector.rotateAroundAxis(up, yaw);
    Location spawnLocation = spawnPosition.toLocation(world).add(transformationVector).add(spawnRotation.clone().multiply(0.0005));
    spawnLocation.setDirection(spawnRotation);
    textDisplay = world.spawn(spawnLocation, TextDisplay.class, this::syncPropertiesWithText);

    super.spawn(world, spawnPosition, spawnRotation);
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

  private void syncPropertiesWithText(TextDisplay entity) {
    entity.text(this.text);
    entity.setDisplayWidth(this.textDisplayWidth);
    entity.setDisplayHeight(this.textDisplayHeight);
    entity.setShadowed(this.isShadowed());
    entity.setShadowRadius(this.getShadowRadius());
    entity.setShadowStrength(this.getShadowStrength());
    entity.setAlignment(this.alignment);
    entity.setLineWidth(this.charactersPerLine);
    entity.setSeeThrough(true);
    entity.setBillboard(Display.Billboard.FIXED);
    entity.setGlowing(false);
  }

}
