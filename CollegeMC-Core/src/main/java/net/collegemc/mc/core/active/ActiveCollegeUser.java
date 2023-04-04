package net.collegemc.mc.core.active;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.mineskin.data.Skin;
import net.collegemc.common.mineskin.data.Texture;
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
import net.collegemc.mc.libs.spigot.NameGenerator;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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

  public void applyProfileSkin() {
    PlayerSkinManager playerSkinManager = CollegeLibrary.getPlayerSkinManager();
    Player player = this.getBukkitPlayer();

    this.getCurrentCollegeProfile().ifPresent(profile -> {
      Skin skin = playerSkinManager.getSkin(profile.getSkinName());
      if (skin != null) {
        PlayerProfile playerProfile = player.getPlayerProfile();
        playerProfile.removeProperty("textures");
        Texture texture = skin.getData().getTexture();
        playerProfile.setProperty(new ProfileProperty("textures", texture.getValue(), texture.getSignature()));
        player.setPlayerProfile(playerProfile);
      }
    });
  }

  public void applyProfileName() {
    NameTagManager tagManager = CollegeLibrary.getNameTagManager();
    Player player = this.getBukkitPlayer();

    this.getCurrentCollegeProfile().ifPresent(profile -> {
      tagManager.tag(player, profile.getName());
    });
  }

  private CompletableFuture<Boolean> applyProfile(CollegeProfile profile) {
    CollegeProfileMetaDataManager metaDataManager = CollegeCore.getCollegeProfileMetaDataManager();

    Player player = this.getBukkitPlayer();

    CollegeProfileMetaData metaData = metaDataManager.getMetaData(profile.getCollegeProfileId());
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    TaskManager.runTask(() -> {
      this.applyProfileSkin();

      if(metaData.getInventoryContent() != null) {
        player.getInventory().setContents(metaData.getInventoryContent());
      }

      if(metaData.getLastKnownLocation() != null) {
        player.teleportAsync(metaData.getLastKnownLocation()).thenAccept(future::complete);
      }

      TaskManager.runTask(this::applyProfileName);
    });

    if(metaData.getLastKnownLocation() == null) {
      future.complete(false);
    }

    return future;
  }

  public CompletableFuture<CollegeProfile> createProfile(String profileName) {
    return createProfile(profileName, null);
  }

  public CompletableFuture<CollegeProfile> createProfile(String profileName, String skinName) {
    NetworkUserManager networkUserManager = GlobalGateway.getNetworkUserManager();
    CollegeProfileManager collegeProfileManager = GlobalGateway.getCollegeProfileManager();
    CollegeProfileMetaDataManager metaDataManager = CollegeCore.getCollegeProfileMetaDataManager();
    return TaskManager.supplyOnIOPool(() -> collegeProfileManager.createProfile(profileName, this.minecraftId, skinName, true))
            .thenApply(profile -> {
              networkUserManager.applyToRemoteUser(this.minecraftId, data -> {
                data.getCollegeProfiles().add(profile.getCollegeProfileId());
              });
              metaDataManager.loadMetaData(profile.getCollegeProfileId());
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
