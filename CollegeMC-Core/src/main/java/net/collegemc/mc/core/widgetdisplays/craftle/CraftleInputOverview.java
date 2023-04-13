package net.collegemc.mc.core.widgetdisplays.craftle;

import lombok.Getter;
import net.collegemc.mc.libs.displaywidgets.Vec2f;
import net.collegemc.mc.libs.displaywidgets.WidgetFrame;
import net.collegemc.mc.libs.displaywidgets.WidgetText;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;

public class CraftleInputOverview {

  private final WidgetText[][] widgets;
  private final Vec2f overviewPosition;
  @Getter
  private int row = 0;
  @Getter
  private int letter = 0;
  @Getter
  private String currentWord = "";
  public static final int INPUT_LETTER_WIDTH = 2;
  public static final int INPUT_LETTER_HEIGHT = 2;

  public CraftleInputOverview(Vec2f overviewPosition) {
    this.overviewPosition = overviewPosition;
    this.widgets = new WidgetText[6][5];
  }

  public void input(Character character, CraftleInputKey.KeyState state) {
    if (row > 6 || letter >= 5) {
      //TODO play no-no sound
      return;
    }
    WidgetText letterWidget = widgets[row][letter++];
    letterWidget.setText(Component.text(character));
    applyState(letterWidget, state);
    currentWord += character;
  }

  public void setState(int letter, int row, CraftleInputKey.KeyState state) {
    applyState(widgets[row][letter], state);
  }

  public void newLine() {
    row++;
    letter = 0;
    currentWord = "";
  }

  public void backspace() {
    if (letter == 0) {
      return;
    }
    WidgetText letterWidget = widgets[row][letter--];
    letterWidget.setText(Component.text("-"));
    letterWidget.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
    letterWidget.update();
  }

  public void construct(WidgetFrame frame) {
    for (int y = 0; y < widgets.length; y++) {
      for (int x = 0; x < widgets[y].length; x++) {
        Vec2f letterPosition = new Vec2f((x * INPUT_LETTER_WIDTH) + overviewPosition.x, (y * INPUT_LETTER_HEIGHT) + overviewPosition.y);
        WidgetText letterWidget = new WidgetText(Component.text("-"), letterPosition, INPUT_LETTER_WIDTH, INPUT_LETTER_HEIGHT, Color.fromARGB(0, 0, 0, 0), 1.0);
        letterWidget.setVerticalAlignment(WidgetText.VerticalAlignment.CENTER);
        frame.addChild(letterWidget);
        widgets[y][x] = letterWidget;
      }
    }
  }

  private void applyState(WidgetText widgetText, CraftleInputKey.KeyState state) {
    widgetText.setBackgroundColor(state.getDefaultColor());
    widgetText.update();
  }
}
