package net.collegemc.mc.libs.displaywidgets;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@CommandAlias("widgetdebug")
public class WidgetDebugCommand extends BaseCommand {

  private WidgetFrame frame;

  @Default
  @CommandCompletion("x y z")
  public void onDefault(Player sender, int x, int y, int z) {
    DisplayWidgetManager widgetManager = CollegeLibrary.getDisplayWidgetManager();
    WidgetFrame frame = new WidgetFrame(0, new Vector(x, y, z), new Vector(0, 0, -1), 4, 4, Color.GREEN, 1.0);
    this.frame = frame;
    /* WidgetButton.ButtonDisplayProperties invisibleProperties = new WidgetButton.ButtonDisplayProperties(Color.fromARGB(255, 0, 255, 0), 0.0);
    WidgetButton button1 = new WidgetButton(1, new Vec2(4, 4), 1, 1, WidgetButton.ButtonType.TOGGLEABLE, invisibleProperties, invisibleProperties);
    WidgetButton button2 = new WidgetButton(3, new Vec2(6, 4), 1, 1, WidgetButton.ButtonType.TOGGLEABLE, invisibleProperties, invisibleProperties);
    button1.setText(Component.text("ꀀ"));
    button1.setVerticalAlignment(WidgetText.VerticalAlignment.CENTER);
    button2.setVerticalAlignment(WidgetText.VerticalAlignment.CENTER);
    button2.setText(Component.text("ꀀ"));
    button1.addEventHandler(WidgetButton.ButtonStateChangedEvent.class, buttonStateChangedEvent -> {
      if (buttonStateChangedEvent.isPressedState()) {
        button1.setText(Component.text("ꀁ"));
        button1.update();
        button2.setText(Component.text("ꀀ"));
        button2.update();
      } else {
        button1.setText(Component.text("ꀀ"));
        button1.update();
      }
    });
    button2.addEventHandler(WidgetButton.ButtonStateChangedEvent.class, buttonStateChangedEvent -> {
      if (buttonStateChangedEvent.isPressedState()) {
        button2.setText(Component.text("ꀁ"));
        button2.update();
        button1.setText(Component.text("ꀀ"));
        button1.update();
      } else {
        button2.setText(Component.text("ꀀ"));
        button2.update();
      }
    });
    frame.addChild(button1);
    frame.addChild(button2);*/
    widgetManager.createWindow(frame, sender.getWorld());
    widgetManager.engage(sender, 0);
  }

  @Subcommand("rotate")
  @CommandCompletion("angle")
  public void onRotate(Player sender, String facing) {
    switch (facing) {
      case "NORTH" -> frame.rotateTo(new Vector(0, 0, -1));
      case "SOUTH" -> frame.rotateTo(new Vector(0, 0, 1));
      case "WEST" -> frame.rotateTo(new Vector(-1, 0, 0));
      case "EAST" -> frame.rotateTo(new Vector(1, 0, 0));
    }
  }

}
