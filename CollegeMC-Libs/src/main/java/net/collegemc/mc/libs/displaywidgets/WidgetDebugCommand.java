package net.collegemc.mc.libs.displaywidgets;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.collegemc.mc.libs.CollegeLibrary;
import net.kyori.adventure.text.Component;
import net.minecraft.world.phys.Vec2;
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
    WidgetFrame frame = new WidgetFrame(0, new Vector(x, y, z), new Vector(0, 0, 1), 16, 16, Color.GREEN, 1.0);
    this.frame = frame;
    WidgetText widgetText = new WidgetText(1, Component.text("Hello World!"), new Vec2(1, 1), 4, 4, Color.BLACK, 1.0);
    frame.addChild(widgetText);
    widgetManager.createWindow(frame, sender.getWorld());
    widgetManager.engage(sender, 0);
  }

  @Subcommand("rotate")
  @CommandCompletion("angle")
  public void onRotate(Player sender, String facing) {
    switch (facing) {
      case "NORTH" -> frame.applyRotationAndPosition(new Vector(-10, 100, 12), new Vector(0, 0, -1));
      case "SOUTH" -> frame.applyRotationAndPosition(new Vector(-10, 100, 12), new Vector(0, 0, 1));
      case "WEST" -> frame.applyRotationAndPosition(new Vector(-10, 100, 12), new Vector(-1, 0, 0));
      case "EAST" -> frame.applyRotationAndPosition(new Vector(-10, 100, 12), new Vector(1, 0, 0));
    }
  }

  @Subcommand("vrot")
  @CommandCompletion("x y z angle")
  public void vonVRot(int x, int y, int z, double angle) {
    CollegeLibrary.getInstance().getLogger().warning("rotated: " + new Vector(x, y, z).rotateAroundY(Math.toRadians(angle)));
  }

}
