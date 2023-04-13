package net.collegemc.mc.libs.displaywidgets.events;


import net.collegemc.mc.libs.displaywidgets.Vec2f;
import org.bukkit.entity.Player;

public class ClickEvent extends WidgetEvent {
  public ClickEvent(Player actor, Vec2f position) {
    super(actor, position);
  }
}
