package net.collegemc.mc.libs.holograms.implementations.protocollib;


import net.collegemc.mc.libs.holograms.abstraction.Hologram;
import net.collegemc.mc.libs.holograms.abstraction.HologramFactory;
import org.bukkit.Location;

public class PlibHologramFactory implements HologramFactory {
  @Override
  public Hologram createHologram(Location location, String hologramName) {
    return new PlibHologram(location, hologramName);
  }
}
