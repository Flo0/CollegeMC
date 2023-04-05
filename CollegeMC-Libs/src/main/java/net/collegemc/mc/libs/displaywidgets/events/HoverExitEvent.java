package net.collegemc.mc.libs.displaywidgets.events;

import org.bukkit.entity.Player;

public class HoverExitEvent extends WidgetEvent {

  public HoverExitEvent(int id, Player actor) {
    super(id, actor);
  }
}
