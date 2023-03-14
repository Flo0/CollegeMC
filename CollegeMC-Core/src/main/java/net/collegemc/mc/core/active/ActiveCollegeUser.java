package net.collegemc.mc.core.active;

import net.collegemc.common.GlobalGateway;
import net.collegemc.common.network.data.college.CollegeProfile;
import net.collegemc.common.network.data.network.NetworkUserData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public record ActiveCollegeUser(UUID minecraftId) {

  @NotNull
  public Player getBukkitPlayer() {
    Player player = Bukkit.getPlayer(this.minecraftId);
    if (player == null) {
      throw new IllegalStateException("Active user with offline player present.");
    }
    return player;
  }

  @NotNull
  public Optional<CollegeProfile> getActiveCollegeProfile() {
    NetworkUserData userData = this.getNetworkUserData();
    UUID activeProfileId = userData.getActiveCollegeProfileId();
    if (activeProfileId == null) {
      return Optional.empty();
    }
    return Optional.of(GlobalGateway.getCollegeProfileManager().get(activeProfileId));
  }

  public NetworkUserData getNetworkUserData() {
    return GlobalGateway.getNetworkUserManager().getLocalCopy(this.minecraftId);
  }

  public String resolveName() {
    return this.getActiveCollegeProfile()
            .map(CollegeProfile::getFullName)
            .or(() -> Optional.ofNullable(Bukkit.getPlayer(this.minecraftId)).map(Player::getName))
            .orElse("Invalid User");
  }

  public boolean hasPermission(String permission) {
    return this.getBukkitPlayer().hasPermission(permission);
  }

}
