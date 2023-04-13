package net.collegemc.mc.core.widgetdisplays.craftle;

import lombok.Getter;
import net.collegemc.mc.libs.displaywidgets.Vec2f;
import net.collegemc.mc.libs.displaywidgets.WidgetButton;
import net.collegemc.mc.libs.displaywidgets.events.ClickEvent;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;

public class CraftleInputKey extends WidgetButton {

  @Getter
  private KeyState state;
  @Getter
  private final Character character;
  private final CraftleDisplay display;

  public final static int KEY_WIDTH = 2;
  public final static int KEY_HEIGHT = 2;
  public final static int AUTORELEASE_TICKS = 5;

  public CraftleInputKey(Vec2f position, Character character, CraftleDisplay display) {
    super(position, KEY_WIDTH, KEY_HEIGHT, ButtonType.CLICKABLE, new ButtonDisplayProperties(KeyState.NOT_GUESSED.defaultColor, 1.0), new ButtonDisplayProperties(KeyState.NOT_GUESSED.pressedColor, 1.0));
    this.character = character;
    this.display = display;
    this.state = KeyState.NOT_GUESSED;
    this.setText(Component.text(String.valueOf(character)));
    this.setVerticalAlignment(VerticalAlignment.CENTER);
    this.setButtonAutoReleaseTicks(AUTORELEASE_TICKS);
  }

  public void setState(KeyState state, boolean update) {
    this.state = state;
    this.setBackgroundColor(state.defaultColor);
    ButtonDisplayProperties newDefault = new ButtonDisplayProperties(this.state.defaultColor, 1.0);
    ButtonDisplayProperties newPressed = new ButtonDisplayProperties(this.state.pressedColor, 1.0);
    this.setDefaultProperties(newDefault);
    this.setPressedProperties(newPressed);
    if (update) {
      this.update();
    }
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    if (isPressed()) {
      display.accept(this.character);
      UtilPlayer.playUIClick(event.getActor());
    }
  }

  public enum KeyState {
    CORRECT_POSITION(Color.GREEN, Color.WHITE),
    IN_WORD(Color.YELLOW, Color.WHITE),
    NOT_IN_WORD(Color.BLACK, Color.WHITE),
    NOT_GUESSED(Color.GRAY, Color.WHITE);

    @Getter
    private final Color defaultColor;
    @Getter
    private final Color pressedColor;

    KeyState(Color defaultColor, Color pressedColor) {
      this.defaultColor = defaultColor;
      this.pressedColor = pressedColor;
    }
  }

}
