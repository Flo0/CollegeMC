package net.collegemc.mc.libs.displaywidgets;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;

public non-sealed class WidgetText extends AbstractWidget {

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
  private TextDisplay.TextAlignment alignment = TextDisplay.TextAlignment.LEFT;
  @Getter
  private WidgetBackground backgroundWidget;

  public WidgetText(Component text, Vec2f position, int width, int height, Color backgroundColor, double opacity) {
    super(position, width, height, Color.fromARGB(0, 0, 0, 0), opacity);
    this.text = text;
    backgroundWidget = new WidgetBackground(new Vec2f(position.x, position.y), width, height, backgroundColor);
    this.children.add(backgroundWidget);
  }

  public WidgetText(Vec2f position, int width, int height, Color backgroundColor, double opacity) {
    this(Component.text(""), position, width, height, backgroundColor, opacity);
  }

  @Override
  public void spawn(World world, Vector spawnPosition) {
    super.spawn(world, spawnPosition.add(new Vector(0, 0, 0)));
    syncPropertiesWithText();
  }

  @Override
  public void update() {
    syncPropertiesWithText();
  }


  @Override
  public void setBackgroundColor(Color backgroundColor) {
    this.getBackgroundWidget().setBackgroundColor(backgroundColor);
  }


  @Override
  public void applyTransformation(Vector worldPosition, Vector facing, boolean passThrough, int depth) {
    super.applyTransformation(worldPosition.clone().add(new Vector(0, getTextYOffset(), 0)), facing.clone(), false, depth);
    backgroundWidget.applyTransformation(worldPosition.add(facing.clone().multiply(AbstractWidget.CHILD_OFFSET)), facing.clone(), true, ++depth);
  }

  private float getTextYOffset() {
    return switch (verticalAlignment) {
      case TOP -> (float) getHeight() / 4 - (float) 1 / 4 * getLines(text.toString());
      case CENTER -> (float) getHeight() / 8 - (float) 1 / 8 * getLines(text.toString());
      case BOTTOM -> 0;
    };
  }

  private int getLines(String text) {
    return (int) text.lines().count();
  }

  @SuppressWarnings("deprecation")
  private void syncPropertiesWithText() {
    getDisplayEntity().text(this.text);
    getDisplayEntity().setShadowed(this.isShadowed());
    getDisplayEntity().setShadowRadius(this.getShadowRadius());
    getDisplayEntity().setShadowStrength(this.getShadowStrength());
    getDisplayEntity().setAlignment(this.alignment);
    getDisplayEntity().setLineWidth(this.charactersPerLine);
    getDisplayEntity().setSeeThrough(false);
    getDisplayEntity().setBillboard(Display.Billboard.FIXED);
    getDisplayEntity().setGlowing(false);
    getDisplayEntity().setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
    getBackgroundWidget().update();
  }

  public enum VerticalAlignment {
    TOP,
    CENTER,
    BOTTOM
  }

  non-sealed static class WidgetBackground extends AbstractWidget {
    protected WidgetBackground(Vec2f position, int width, int height, Color backgroundColor) {
      super(position, width, height, backgroundColor);
    }
  }

}
