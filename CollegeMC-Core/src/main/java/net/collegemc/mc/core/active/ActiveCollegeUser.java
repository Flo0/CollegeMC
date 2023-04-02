package net.collegemc.mc.core.active;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.network.data.college.CollegeProfile;
import net.collegemc.common.network.data.college.CollegeProfileManager;
import net.collegemc.common.network.data.college.ProfileId;
import net.collegemc.common.network.data.network.NetworkUserData;
import net.collegemc.common.network.data.network.NetworkUserManager;
import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.hooks.itemsadder.ItemsAdderHook;
import net.collegemc.mc.libs.nametag.NameTagManager;
import net.collegemc.mc.libs.skinclient.PlayerSkinManager;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.mineskin.data.Skin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record ActiveCollegeUser(UUID minecraftId) {

  public static ActiveCollegeUser of(Player player) {
    return of(player.getUniqueId());
  }

  public static ActiveCollegeUser of(UUID minecraftId) {
    return CollegeCore.getActiveCollegeUserManager().get(minecraftId);
  }

  @NotNull
  public Player getBukkitPlayer() {
    Player player = Bukkit.getPlayer(this.minecraftId);
    if (player == null) {
      throw new IllegalStateException("Active user with offline player present.");
    }
    return player;
  }

  public List<CollegeProfile> getProfileList() {
    NetworkUserData userData = this.getNetworkUserData();
    CollegeProfileManager profileManager = GlobalGateway.getCollegeProfileManager();
    return userData.getCollegeProfiles().stream().map(profileManager::getLoaded).toList();
  }

  @NotNull
  public Optional<CollegeProfile> getCurrentCollegeProfile() {
    NetworkUserData userData = this.getNetworkUserData();
    ProfileId activeProfileId = userData.getActiveCollegeProfileId();
    if (activeProfileId == null) {
      return Optional.empty();
    }
    return Optional.of(GlobalGateway.getCollegeProfileManager().getLoaded(activeProfileId));
  }

  @NotNull
  public Optional<CollegeProfileMetaData> getCurrentMetaData() {
    CollegeProfileMetaDataManager metaDataManager = CollegeCore.getCollegeProfileMetaDataManager();
    return this.getCurrentCollegeProfile()
            .map(CollegeProfile::getCollegeProfileId)
            .map(metaDataManager::getMetaData);
  }

  public CompletableFuture<Void> switchCollegeProfile(ProfileId profileId) {
    NetworkUserManager networkUserManager = GlobalGateway.getNetworkUserManager();
    CollegeProfileManager collegeProfileManager = GlobalGateway.getCollegeProfileManager();

    CollegeProfile profile = collegeProfileManager.getLoaded(profileId);

    if (profile == null) {
      new IllegalStateException("Profile does not exist: " + profileId.getUid()).printStackTrace();
      return CompletableFuture.completedFuture(null);
    }

    return TaskManager.runOnIOPool(() -> {
      networkUserManager.applyToRemoteUser(this.minecraftId, data -> data.setActiveCollegeProfileId(profileId));
    }).thenRun(() -> {
      this.applyProfile(profile).join();
    });
  }

  private CompletableFuture<Boolean> applyProfile(CollegeProfile profile) {
    CollegeProfileMetaDataManager metaDataManager = CollegeCore.getCollegeProfileMetaDataManager();
    NameTagManager tagManager = CollegeLibrary.getNameTagManager();
    PlayerSkinManager playerSkinManager = CollegeLibrary.getPlayerSkinManager();
    Skin skin = playerSkinManager.getSkin(profile.getSkinName());

    Player player = this.getBukkitPlayer();
    ItemsAdderHook.blackFade(player, 7, 14, 7);

    tagManager.tag(player, profile.getName());

    if (skin != null) {
      PlayerProfile playerProfile = player.getPlayerProfile();
      playerProfile.removeProperty("textures");
      playerProfile.setProperty(new ProfileProperty("textures", skin.data.texture.value, skin.data.texture.signature));
      player.setPlayerProfile(playerProfile);
    }

    CollegeProfileMetaData metaData = metaDataManager.getMetaData(profile.getCollegeProfileId());
    player.getInventory().setContents(metaData.getInventoryContent());
    return player.teleportAsync(metaData.getLastKnownLocation());
  }

  public CompletableFuture<CollegeProfile> createProfile(String profileName) {
    NetworkUserManager networkUserManager = GlobalGateway.getNetworkUserManager();
    CollegeProfileManager collegeProfileManager = GlobalGateway.getCollegeProfileManager();
    return TaskManager.supplyOnIOPool(() -> collegeProfileManager.createProfile(profileName, this.minecraftId, true))
            .thenApply(profile -> {
              networkUserManager.applyToRemoteUser(this.minecraftId, data -> {
                data.getCollegeProfiles().add(profile.getCollegeProfileId());
              });
              return profile;
            });
  }

  public NetworkUserData getNetworkUserData() {
    return GlobalGateway.getNetworkUserManager().getLocalCopy(this.minecraftId);
  }

  public String resolveName() {
    return this.getCurrentCollegeProfile()
            .map(CollegeProfile::getName)
            .or(() -> Optional.ofNullable(Bukkit.getPlayer(this.minecraftId)).map(Player::getName))
            .orElse("Invalid User");
  }

  public boolean hasPermission(String permission) {
    return this.getBukkitPlayer().hasPermission(permission);
  }

}
