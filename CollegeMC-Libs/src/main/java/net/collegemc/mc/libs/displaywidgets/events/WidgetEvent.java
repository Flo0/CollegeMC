package net.collegemc.mc.libs.displaywidgets.events;

import lombok.Getter;
import net.minecraft.world.phys.Vec2;
import org.bukkit.entity.Player;

public abstract class WidgetEvent {

  @Getter
  private final Player actor;
  @Getter
  private final Vec2 position;

  public WidgetEvent(Player actor, Vec2 position) {
    this.actor = actor;
    this.position = position;
  }

}
