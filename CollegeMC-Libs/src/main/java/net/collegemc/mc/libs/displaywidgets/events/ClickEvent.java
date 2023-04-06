package net.collegemc.mc.libs.displaywidgets.events;

import net.minecraft.world.phys.Vec2;
import org.bukkit.entity.Player;

public class ClickEvent extends WidgetEvent {
  public ClickEvent(Player actor, Vec2 position) {
    super(actor, position);
  }
}
