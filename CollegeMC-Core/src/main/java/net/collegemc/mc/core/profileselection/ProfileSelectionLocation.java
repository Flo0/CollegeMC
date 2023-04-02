package net.collegemc.mc.core.profileselection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.collegemc.common.mongodb.Identity;
import org.bukkit.Location;

@Getter
@RequiredArgsConstructor
public class ProfileSelectionLocation implements Identity<String> {

  @Setter
  private transient boolean occupied;
  private final String name;
  private final Location playerLocation;
  private final Location profileLocation;

  @Override
  public String getIdentity() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ProfileSelectionLocation selectionLocation)) {
      return false;
    }
    return this.name.equals(selectionLocation.name);
  }
}
