package net.collegemc.mc.libs.holograms.implementations.nms;


import net.collegemc.mc.libs.holograms.abstraction.AbstractHologram;
import net.collegemc.mc.libs.holograms.abstraction.HologramLine;
import org.bukkit.Location;

public class NMSHologram extends AbstractHologram {

  public NMSHologram(Location location, String name) {
    super(location, name);
  }

  @Override
  protected HologramLine createLine(Location location) {
    return new NMSHologramLine(location);
  }

}
