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
    WidgetFrame frame = new WidgetFrame(0, new Vector(x, y, z), sender.getLocation().getDirection(), width, height, Color.GRAY);
    WidgetText text = new WidgetText(1, Component.text("Hello World!"), new Vec2(0, 0), 8, 4, Color.BLUE, 1.0);
    text.setVerticalAlignment(alignment);
    frame.addChild(text);
    widgetManager.createWindow(frame, sender.getWorld());
    widgetManager.engage(sender, frame.id);
  }

}
