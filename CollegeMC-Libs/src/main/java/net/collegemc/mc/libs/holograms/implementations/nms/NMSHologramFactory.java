package net.collegemc.mc.libs.holograms.implementations.nms;

import net.collegemc.mc.libs.holograms.abstraction.Hologram;
import net.collegemc.mc.libs.holograms.abstraction.HologramFactory;
import org.bukkit.Location;

public class NMSHologramFactory implements HologramFactory {
  @Override
  public Hologram createHologram(Location location, String hologramName) {
    return new NMSHologram(location, hologramName);
  }
}
