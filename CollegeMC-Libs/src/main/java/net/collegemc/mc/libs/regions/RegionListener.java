package net.collegemc.mc.libs.regions;

import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.holograms.abstraction.Hologram;
import net.collegemc.mc.libs.regions.permissions.RegionPermission;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

public class RegionListener implements Listener {

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    CollegeLibrary.getRegionManager().removeSelection(event.getPlayer());
  }

  @EventHandler
  public void onClick(PlayerInteractEvent event) {
    Location interactionPoint = event.getInteractionPoint();
    if (interactionPoint == null) {
      return;
    }
    AbstractRegion region = CollegeLibrary.getRegionManager().getRegionWithHighestPriorityAt(interactionPoint);
    if (region == null) {
      return;
    }
    if (region.getPermissionContainer().hasPermission(event.getPlayer().getUniqueId(), RegionPermission.INTERACT)) {
      return;
    }
    event.setCancelled(true);
    Vector middle = interactionPoint.toVector().subtract(event.getPlayer().getEyeLocation().toVector()).multiply(0.5);
    Location holoLoc = event.getPlayer().getEyeLocation().clone().add(middle);
    long stamp = System.nanoTime();
    String name = "_DISPLAY_" + stamp;
    Hologram hologram = CollegeLibrary.getHologramManager().createHologram(holoLoc, name);
    hologram.addLine("§c✖");
    TaskManager.runTaskLater(() -> CollegeLibrary.getHologramManager().deleteHologram(name), 10);
  }

}
