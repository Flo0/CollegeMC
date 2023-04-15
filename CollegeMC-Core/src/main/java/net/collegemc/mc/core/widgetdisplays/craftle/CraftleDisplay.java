package net.collegemc.mc.core.widgetdisplays.craftle;

import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.libs.displaywidgets.Vec2f;
import net.collegemc.mc.libs.displaywidgets.WidgetButton;
import net.collegemc.mc.libs.displaywidgets.WidgetFrame;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.util.Vector;

public class CraftleDisplay extends WidgetFrame {

  private final String word;
  private final CraftleKeyboard craftleKeyboard;
  private final CraftleInputOverview craftleOverview;
  private WidgetButton enterButton;
  private static final WidgetButton.ButtonDisplayProperties enterButtonDefault = new WidgetButton.ButtonDisplayProperties(Color.fromRGB(38, 40, 46), 1.0);
  private static final WidgetButton.ButtonDisplayProperties enterButtonPressed = new WidgetButton.ButtonDisplayProperties(Color.WHITE, 1.0);
  private static final int enterButtonWidth = 2;
  private static final int enterButtonHeight = 3;
  private static final int craftleWidth = 8 * 4;
  private static final int craftleHeight = 4 * 4;
  private static final Vec2f keyboardPosition = new Vec2f(3 * 4, 0);
  private static final Vec2f overviewPosition = new Vec2f(0, 0);
  private static final Vec2f enterPosition = new Vec2f(2 * 4 + 2, 2 * 4);
  private static final Color backgroundColor = Color.fromARGB(150, 43, 45, 48);


  public CraftleDisplay(int id, Vector worldPosition, Vector rotation, String word) {
    super(id, worldPosition, rotation, craftleWidth, craftleHeight, backgroundColor);
    this.craftleKeyboard = new CraftleKeyboard(this, keyboardPosition);
    this.craftleOverview = new CraftleInputOverview(overviewPosition);
    this.word = word;
    this.craftleKeyboard.construct(this);
    this.craftleOverview.construct(this);
    this.construct();
  }


  public void accept(Character character) {
    if (craftleOverview.getLetter() >= 5) {
      return;
    }

    craftleOverview.input(character, CraftleInputKey.KeyState.NOT_GUESSED);
  }

  public void enterWord() {
    if (craftleOverview.getLetter() < 4) {
      return;
    }
    String inputWord = craftleOverview.getCurrentWord();
    inputWord = inputWord.toLowerCase();
    if (!CollegeCore.getCraftleManager().validWord(inputWord)) {
      //TODO user feedback
      return;
    }
    char[] inputChars = inputWord.toCharArray();
    char[] wordChars = word.toCharArray();
    int currentRow = craftleOverview.getRow();
    int currentLetter = craftleOverview.getLetter();

    for (int i = 0; i < inputChars.length; i++) {
      char inputChar = inputChars[i];
      CraftleInputKey.KeyState state;
      if (inputChar == wordChars[i]) {
        state = CraftleInputKey.KeyState.CORRECT_POSITION;
      } else if (word.contains(String.valueOf(inputChar))) {
        int count = 0;
        for (int j = 0; j < inputChars.length; j++) {
          if (inputChars[j] == inputChar) {
            count++;
          }
        }
        if (count >= 2) {
          state = CraftleInputKey.KeyState.IN_WORD;
        } else {
          state = CraftleInputKey.KeyState.NOT_IN_WORD;
        }

      } else {
        state = CraftleInputKey.KeyState.NOT_IN_WORD;
      }

      if (craftleKeyboard.getState(inputChar) == CraftleInputKey.KeyState.NOT_GUESSED) {
        craftleKeyboard.setState(inputChar, state);
      }
      craftleOverview.setState(currentLetter - (4 - i) - 1, currentRow, state);
    }

    if (inputWord.equals(word)) {

    }

    craftleOverview.newLine();
    if (craftleOverview.getRow() >= 6) {
      //TODO fail-state
    }
  }

  public void backspace() {
    craftleOverview.backspace();
  }

  private void construct() {
    WidgetButton enterButton = new WidgetButton(enterPosition, enterButtonWidth, enterButtonHeight, WidgetButton.ButtonType.CLICKABLE, enterButtonDefault, enterButtonPressed);
    enterButton.setText(Component.text("↲"));
    enterButton.addEventHandler(WidgetButton.ButtonStateChangedEvent.class, buttonStateChangedEvent -> {
      if (buttonStateChangedEvent.isPressedState()) {
        this.enterWord();
      }
    });
    this.addChild(enterButton);
  }


}
