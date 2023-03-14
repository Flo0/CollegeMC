package net.collegemc.mc.libs.regions;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public class RegionSelection {
  
  private Location first;
  private Location second;

  public boolean valid() {
    return this.first != null && this.second != null && this.first.getWorld() == this.second.getWorld();
  }

}
