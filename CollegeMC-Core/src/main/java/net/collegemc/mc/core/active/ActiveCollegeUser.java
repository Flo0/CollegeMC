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
import net.collegemc.mc.core.economy.EconomyAccount;
import net.collegemc.mc.core.economy.EconomyManager;
import net.collegemc.mc.core.economy.EconomyOperation;
import net.collegemc.mc.core.economy.EconomyTransaction;
import net.collegemc.mc.core.economy.EconomyTransactionResult;
import net.collegemc.mc.core.friends.FriendsList;
import net.collegemc.mc.core.quests.QuestList;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.hooks.itemsadder.CollegemcSound;
import net.collegemc.mc.libs.hooks.itemsadder.CollegemcSymbol;
import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.nametag.NameTagManager;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  @Nullable
  public EconomyAccount getEconomyAccountSnapshot() {
    EconomyManager economyManager = CollegeCore.getEconomyManager();
    return getCurrentCollegeProfile()
            .map(CollegeProfile::getCollegeProfileId)
            .map(economyManager::getAccountSnapshot)
            .orElse(null);
  }

  public CompletableFuture<EconomyTransactionResult> applyToEconomyAccount(EconomyOperation operation, double amount) {
    EconomyManager economyManager = CollegeCore.getEconomyManager();
    return getCurrentCollegeProfile().map(profile -> {
      ProfileId profileId = profile.getCollegeProfileId();
      EconomyTransaction economyTransaction = new EconomyTransaction(profileId, operation, amount);
      return TaskManager.supplyOnIOPool(() -> economyManager.applyTransaction(economyTransaction));
    }).orElse(CompletableFuture.completedFuture(EconomyTransactionResult.FAILED)).thenApply((result) -> {
      if (!result.isSuccess()) {
        return result;
      }
      Player player = this.getBukkitPlayer();
      if (player != null) {
        boolean addOperation = operation == EconomyOperation.ADD;
        String prefix = addOperation ? "§a+" : "§c-";
        String message = prefix + amount + " §f" + CollegemcSymbol.COIN.get();

        Msg.sendInfo(player, message);
        if (addOperation) {
          CollegemcSound.CACHING.play(player, 0.66F, 1.0F);
        }
      }
      return result;
    });
  }

  @Nullable
  public QuestList getQuestList() {
    return this.getCurrentCollegeProfile().map(profile -> {
      return CollegeCore.getQuestManager().getQuestList(profile.getCollegeProfileId());
    }).orElse(null);
  }

  @Nullable
  public FriendsList getFriendList() {
    return this.getCurrentCollegeProfile().map(profile -> {
      return CollegeCore.getFriendsManager().getActiveFriendsList(profile.getCollegeProfileId());
    }).orElse(null);
  }

  public Player getBukkitPlayer() {
    return Bukkit.getPlayer(this.minecraftId);
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
    Player player = this.getBukkitPlayer();
    this.getCurrentCollegeProfile().ifPresent(profile -> {
      Skin skin = profile.getSkin();
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
      tagManager.untag(player.getEntityId());
      tagManager.tag(player, profile.getName());
    });
  }

  private CompletableFuture<Boolean> applyProfile(CollegeProfile profile) {
    CollegeProfileMetaDataManager metaDataManager = CollegeCore.getCollegeProfileMetaDataManager();

    Player player = this.getBukkitPlayer();

    CollegeProfileMetaData metaData = metaDataManager.getMetaData(profile.getCollegeProfileId());
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    TaskManager.runTask(() -> {
      if (metaData.getLastKnownLocation() != null) {
        player.teleportAsync(metaData.getLastKnownLocation()).thenRun(() -> {
          this.applyProfileSkin();

          if (metaData.getInventoryContent() != null) {
            player.getInventory().setContents(metaData.getInventoryContent());
          }

          TaskManager.runTask(this::applyProfileName);
        });
      } else {
        this.applyProfileSkin();

        if (metaData.getInventoryContent() != null) {
          player.getInventory().setContents(metaData.getInventoryContent());
        }

        TaskManager.runTask(this::applyProfileName);
      }
    });

    if (metaData.getLastKnownLocation() == null) {
      future.complete(false);
    }

    return future;
  }

  public CompletableFuture<CollegeProfile> createProfile(String profileName) {
    return createProfile(profileName, null);
  }

  public CompletableFuture<CollegeProfile> createProfile(String profileName, Skin skin) {
    NetworkUserManager networkUserManager = GlobalGateway.getNetworkUserManager();
    CollegeProfileManager collegeProfileManager = GlobalGateway.getCollegeProfileManager();
    CollegeProfileMetaDataManager metaDataManager = CollegeCore.getCollegeProfileMetaDataManager();
    return TaskManager.supplyOnIOPool(() -> collegeProfileManager.createProfile(profileName, this.minecraftId, skin, true))
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
