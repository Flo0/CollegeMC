package net.collegemc.mc.libs.holograms.abstraction;

import org.bukkit.Location;

public interface HologramFactory {

  Hologram createHologram(Location location, String hologramName);

}
