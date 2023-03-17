package net.collegemc.mc.libs.regions.permissions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@AllArgsConstructor
public enum RegionPermission {
  
  INTERACT("Interact", new ItemStack(Material.OAK_DOOR));

  private final String displayName;
  private final ItemStack displayIcon;

}
