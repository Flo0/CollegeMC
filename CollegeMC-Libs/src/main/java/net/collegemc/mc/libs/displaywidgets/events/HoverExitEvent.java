package net.collegemc.mc.libs.displaywidgets.events;

import net.collegemc.mc.libs.displaywidgets.Vec2f;
import org.bukkit.entity.Player;

public class HoverExitEvent extends WidgetEvent {

  public HoverExitEvent(Player actor, Vec2f position) {
    super(actor, position);
  }
}
