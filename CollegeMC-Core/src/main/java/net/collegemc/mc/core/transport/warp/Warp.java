package net.collegemc.mc.core.transport.warp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

@NoArgsConstructor
@AllArgsConstructor
public class Warp {

  @Getter
  private Location location;
  @Getter
  private String name;
  @Getter
  @Setter
  private ItemStack icon;

}
