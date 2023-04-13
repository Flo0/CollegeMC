package net.collegemc.mc.libs.displaywidgets.events;

import lombok.Getter;
import net.collegemc.mc.libs.displaywidgets.Vec2f;
import org.bukkit.entity.Player;

public abstract class WidgetEvent {

  @Getter
  private final Player actor;
  @Getter
  private final Vec2f position;

  public WidgetEvent(Player actor, Vec2f position) {
    this.actor = actor;
    this.position = position;
  }

}
