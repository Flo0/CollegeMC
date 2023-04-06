package net.collegemc.mc.libs.displaywidgets;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.collegemc.mc.libs.CollegeLibrary;
import net.kyori.adventure.text.Component;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Color;
import org.bukkit.entity.Player;

@CommandAlias("widgetdebug")
public class WidgetDebugCommand extends BaseCommand {

  @Default
  public void onDefault(Player sender) {
    DisplayWidgetManager widgetManager = CollegeLibrary.getDisplayWidgetManager();
    WidgetFrame frame = new WidgetFrame(0, sender.getLocation().toVector(), sender.getLocation().getDirection(), 16, 16, Color.GRAY);

    WidgetButton.ButtonDisplayProperties defaultProperty = new WidgetButton.ButtonDisplayProperties(Color.BLUE, 1.0d);
    WidgetButton.ButtonDisplayProperties pressedProperty = new WidgetButton.ButtonDisplayProperties(Color.GREEN, 0.9d);
    WidgetButton button = new WidgetButton(1, new Vec2(1, 1), 10, 10, WidgetButton.ButtonType.CLICKABLE, defaultProperty, pressedProperty);
    button.setText(Component.text("Click me!"));
    button.addEventHandler(WidgetButton.ButtonStateChangedEvent.class, event -> {
      if (event.isPressedState()) {
        sender.sendMessage("Button pressed!");
      }
    });
    frame.addChild(button);

    widgetManager.createWindow(frame, sender.getWorld());
    widgetManager.engage(sender, frame.id);
  }

}
