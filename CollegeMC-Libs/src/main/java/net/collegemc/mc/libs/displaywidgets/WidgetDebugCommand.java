package net.collegemc.mc.libs.displaywidgets;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Values;
import net.collegemc.mc.libs.CollegeLibrary;
import net.kyori.adventure.text.Component;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@CommandAlias("widgetdebug")
public class WidgetDebugCommand extends BaseCommand {

  @Default
  @CommandCompletion("@VerticalAlignment width height x y z")
  public void onDefault(Player sender, @Values("@VerticalAlignment") WidgetText.VerticalAlignment alignment, int width, int height, int x, int y, int z) {
    DisplayWidgetManager widgetManager = CollegeLibrary.getDisplayWidgetManager();
    WidgetFrame frame = new WidgetFrame(0, new Vector(x, y, z), sender.getLocation().getDirection(), 20, 12, Color.GRAY);
    WidgetText text = new WidgetText(1, Component.text("Counter Example"), new Vec2(1, 1), 10, 2, Color.BLUE, 1.0);
    text.setVerticalAlignment(alignment);
    frame.addChild(text);
    final Count count = new Count();
    WidgetButton.ButtonDisplayProperties defaultProperties = new WidgetButton.ButtonDisplayProperties(Color.BLUE, -1.0);
    WidgetButton.ButtonDisplayProperties clickedProperties1 = new WidgetButton.ButtonDisplayProperties(Color.GREEN, -1.0);
    WidgetButton.ButtonDisplayProperties clickedProperties2 = new WidgetButton.ButtonDisplayProperties(Color.PURPLE, -1.0);
    WidgetButton button1 = new WidgetButton(3, new Vec2(1, 4), 2, 2, WidgetButton.ButtonType.CLICKABLE, defaultProperties, clickedProperties1);
    WidgetButton button2 = new WidgetButton(5, new Vec2(6, 4), 2, 2, WidgetButton.ButtonType.CLICKABLE, defaultProperties, clickedProperties2);
    WidgetText countDisplay = new WidgetText(7, Component.text("Count: " + count.count), new Vec2(10, 4), 6, 2, Color.RED, 1.0);
    button1.setText(Component.text("+1"));
    button2.setText(Component.text("-1"));
    button1.addEventHandler(WidgetButton.ButtonStateChangedEvent.class, buttonStateChangedEvent -> {
      if (buttonStateChangedEvent.isPressedState()) {
        count.count++;
        countDisplay.setText(Component.text("Count: " + count.count));
        countDisplay.update();
      }
    });
    button2.addEventHandler(WidgetButton.ButtonStateChangedEvent.class, buttonStateChangedEvent -> {
      if (buttonStateChangedEvent.isPressedState()) {
        count.count--;
        countDisplay.setText(Component.text("Count: " + count.count));
        countDisplay.update();
      }
    });
    frame.addChild(button1);
    frame.addChild(button2);
    frame.addChild(countDisplay);
    text.setVerticalAlignment(alignment);
    widgetManager.createWindow(frame, sender.getWorld());
    widgetManager.engage(sender, frame.id);
  }

  private static class Count {
    public int count = 0;
  }

}
