package net.collegemc.mc.libs.displaywidgets.events;

import lombok.Getter;
import org.bukkit.entity.Player;

public abstract class WidgetEvent {

  @Getter
  private final int id;
  @Getter
  private final Player actor;

  public WidgetEvent(int id, Player actor) {
    this.id = id;
    this.actor = actor;
  }

}
