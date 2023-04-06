package net.collegemc.mc.libs.displaywidgets.events;

import net.minecraft.world.phys.Vec2;
import org.bukkit.entity.Player;

public class HoverEnterEvent extends WidgetEvent {

  public HoverEnterEvent(Player actor, Vec2 position) {
    super(actor, position);
  }

}
