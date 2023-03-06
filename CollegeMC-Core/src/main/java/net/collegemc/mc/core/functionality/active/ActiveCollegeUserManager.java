package net.collegemc.mc.core.functionality.active;

import com.google.common.base.Preconditions;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.network.data.college.CollegeProfileManager;
import net.collegemc.common.network.data.network.NetworkUserData;
import net.collegemc.common.network.data.network.NetworkUserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveCollegeUserManager {

  private final Map<UUID, ActiveCollegeUser> activeCollegeUserMap = new ConcurrentHashMap<>();

  public List<String> getUserNames() {
    return this.activeCollegeUserMap.values().stream().map(ActiveCollegeUser::resolveName).toList();
  }

  public ActiveCollegeUser getByName(String userName) {
    for (ActiveCollegeUser collegeUser : this.activeCollegeUserMap.values()) {
      if (collegeUser.resolveName().equals(userName)) {
        return collegeUser;
      }
    }
    return null;
  }

  public void load(UUID userId, String name) {
    Preconditions.checkState(!this.activeCollegeUserMap.containsKey(userId), "Tried loading already loaded user.");
    ActiveCollegeUser collegeUser = new ActiveCollegeUser(userId);
    this.activeCollegeUserMap.put(userId, collegeUser);

    NetworkUserManager networkUserManager = GlobalGateway.getNetworkUserManager();
    CollegeProfileManager collegeProfileManager = GlobalGateway.getCollegeProfileManager();

    List<UUID> collegeProfiles = new ArrayList<>();

    networkUserManager.applyToRemoteUser(userId, user -> {
      user.setLastSeenMinecraftName(name);
      user.setMinecraftUid(userId);
      user.setLastLoginTimestamp(System.currentTimeMillis());
      collegeProfiles.addAll(user.getCollegeProfiles());
    });

    networkUserManager.cache(userId);
    collegeProfiles.forEach(collegeProfileManager::load);
  }

  public void unload(UUID userId) {
    ActiveCollegeUser activeCollegeUser = this.activeCollegeUserMap.remove(userId);
    Preconditions.checkState(activeCollegeUser != null, "Tried unloading missing data.");

    CollegeProfileManager collegeProfileManager = GlobalGateway.getCollegeProfileManager();
    NetworkUserManager networkUserManager = GlobalGateway.getNetworkUserManager();

    CompletableFuture.runAsync(() -> {
      NetworkUserData networkUserData = networkUserManager.getLocalCopy(userId);
      networkUserData.getCollegeProfiles().forEach(collegeProfileManager::unload);
      networkUserManager.uncache(userId);
    });
  }

  public ActiveCollegeUser get(UUID userId) {
    return this.activeCollegeUserMap.get(userId);
  }

}
