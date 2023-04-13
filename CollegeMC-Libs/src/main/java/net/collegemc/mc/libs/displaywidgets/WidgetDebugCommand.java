package net.collegemc.mc.libs.displaywidgets;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.collegemc.mc.libs.CollegeLibrary;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

@CommandAlias("widgetdebug")
public class WidgetDebugCommand extends BaseCommand {


  @Default
  public void onDefault(Player sender) {
    WidgetFrame frame = new WidgetFrame(1, new Vector(-8, 105, 26), new Vector(0, 0, 1), 36, 36, Color.fromARGB(155, 30, 31, 34));

    WidgetText center = new WidgetText(Component.text("ꀇ"), new Vec2f(16, 16), 4, 4, Color.fromARGB(155, 0, 0, 255), 1.0);
    center.setVerticalAlignment(WidgetText.VerticalAlignment.CENTER);

    frame.addChild(center);

    DisplayWidgetManager widgetManager = CollegeLibrary.getDisplayWidgetManager();
    widgetManager.createWindow(frame, sender.getWorld());
    widgetManager.engage(sender, 1);
  }

  @Subcommand("kill")
  public void onKill(Player sender) {
    sender.performCommand("/kill @e[type=!minecraft:player]");
  }


}
