package net.collegemc.mc.libs.displaywidgets.events;

import org.bukkit.entity.Player;

public class ClickEvent extends WidgetEvent {
  public ClickEvent(int id, Player actor) {
    super(id, actor);
  }
}
