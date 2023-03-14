package net.collegemc.mc.core.transport.warp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Location;

@NoArgsConstructor
@AllArgsConstructor
public class Warp {

  @Getter
  private Location location;
  @Getter
  private String name;

}
