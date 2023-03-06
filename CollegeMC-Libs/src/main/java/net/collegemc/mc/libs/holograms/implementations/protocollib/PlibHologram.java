package net.collegemc.mc.libs.holograms.implementations.protocollib;

import net.collegemc.mc.libs.holograms.abstraction.AbstractHologram;
import net.collegemc.mc.libs.holograms.abstraction.HologramLine;
import org.bukkit.Location;

public class PlibHologram extends AbstractHologram {
  public PlibHologram(Location location, String name) {
    super(location, name);
  }

  @Override
  protected HologramLine createLine(Location location) {
    return new PlibHologramLine(location);
  }
}
