package net.collegemc.mc.core.widgetdisplays.craftle;


import net.collegemc.mc.libs.displaywidgets.Vec2f;
import net.collegemc.mc.libs.displaywidgets.WidgetButton;
import net.collegemc.mc.libs.displaywidgets.WidgetFrame;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;


public class CraftleKeyboard {

  private final CraftleInputKey[][] keys;
  private final Vec2f keyboardPosition;
  private final CraftleDisplay display;
  private static final int[] characterDistribution = {10, 9, 7};
  private static final WidgetButton.ButtonDisplayProperties backspaceDefaultProperty = new WidgetButton.ButtonDisplayProperties(Color.fromRGB(38, 40, 46), 1.0);
  private static final WidgetButton.ButtonDisplayProperties backspacePressedProperty = new WidgetButton.ButtonDisplayProperties(Color.WHITE, 1.0);
  private static final int backspaceOffset = 1;

  public CraftleKeyboard(CraftleDisplay display, Vec2f keyboardPosition) {
    this.keys = new CraftleInputKey[3][];
    this.display = display;
    this.keyboardPosition = keyboardPosition;
  }

  public void setState(Character character, CraftleInputKey.KeyState state) {
    Qwerty keyboardPosition = Qwerty.valueOf(character.toString().toUpperCase());
    keys[keyboardPosition.y][keyboardPosition.x].setState(state, true);
  }

  public CraftleInputKey.KeyState getState(Character character) {
    Qwerty keyboardPosition = Qwerty.valueOf(character.toString().toUpperCase());
    return keys[keyboardPosition.y][keyboardPosition.x].getState();
  }

  public void construct(WidgetFrame frame) {
    int index = 0;
    for (int y = 0; y < keys.length; y++) {
      this.keys[y] = new CraftleInputKey[characterDistribution[y]];
      for (int x = 0; x < characterDistribution[y]; x++) {
        Vec2f keyPosition = new Vec2f((x * CraftleInputKey.KEY_WIDTH) + keyboardPosition.x + y, (y * CraftleInputKey.KEY_HEIGHT) + keyboardPosition.y);
        Character character = Qwerty.values()[index++].character;
        CraftleInputKey input = new CraftleInputKey(keyPosition, character, display);
        keys[y][x] = input;
        frame.addChild(input);
      }
    }

    Vec2f backspacePosition = new Vec2f((7 * CraftleInputKey.KEY_WIDTH) + keyboardPosition.x + 1 + backspaceOffset, 2 * CraftleInputKey.KEY_HEIGHT + keyboardPosition.y);
    WidgetButton backspace = new WidgetButton(backspacePosition, 4, 2, WidgetButton.ButtonType.CLICKABLE, backspaceDefaultProperty, backspacePressedProperty);
    backspace.setButtonAutoReleaseTicks(CraftleInputKey.AUTORELEASE_TICKS);
    backspace.setText(Component.text("←"));
    backspace.addEventHandler(WidgetButton.ButtonStateChangedEvent.class, event -> {
      if (event.isPressedState()) {
        display.backspace();
        UtilPlayer.playUIClick(event.getActor());
      }
    });
    frame.addChild(backspace);
  }


  private enum Qwerty {
    Q('Q', 0, 0),
    W('W', 0, 1),
    E('E', 0, 2),
    R('R', 0, 3),
    T('T', 0, 4),
    Y('Y', 0, 5),
    U('U', 0, 6),
    I('I', 0, 7),
    O('O', 0, 8),
    P('P', 0, 9),
    A('A', 1, 0),
    S('S', 1, 1),
    D('D', 1, 2),
    F('F', 1, 3),
    G('G', 1, 4),
    H('H', 1, 5),
    J('J', 1, 6),
    K('K', 1, 7),
    L('L', 1, 8),
    Z('Z', 2, 0),
    X('X', 2, 1),
    C('C', 2, 2),
    V('V', 2, 3),
    B('B', 2, 4),
    N('N', 2, 5),
    M('M', 2, 6);


    private final char character;
    private final int y;
    private final int x;


    Qwerty(char character, int y, int x) {
      this.character = character;
      this.y = y;
      this.x = x;
    }
  }
}
