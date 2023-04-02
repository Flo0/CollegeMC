package net.collegemc.mc.core.active;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

@Data
public class CollegeProfileMetaData {

  private Location lastKnownLocation;
  private ItemStack[] inventoryContent;

}
