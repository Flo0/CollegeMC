package net.collegemc.mc.libs.displaywidgets;

import io.papermc.paper.event.player.PlayerArmSwingEvent;
import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WidgetListener implements Listener {

  @EventHandler
  public void onWidgetClick(PlayerArmSwingEvent event) {
    CollegeLibrary.getDisplayWidgetManager().onSwing(event);
  }

}
