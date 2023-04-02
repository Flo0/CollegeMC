package net.collegemc.mc.core.active;

import com.google.common.base.Preconditions;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.network.data.college.CollegeProfileManager;
import net.collegemc.common.network.data.college.ProfileId;
import net.collegemc.common.network.data.network.NetworkUserData;
import net.collegemc.common.network.data.network.NetworkUserManager;
import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.libs.tasks.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveCollegeUserManager {

  private final Map<UUID, ActiveCollegeUser> activeCollegeUserMap;

  public ActiveCollegeUserManager() {
    this.activeCollegeUserMap = new ConcurrentHashMap<>();
  }

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

  public void loadData(UUID userId, String name) {
    Preconditions.checkState(!this.activeCollegeUserMap.containsKey(userId), "Tried loading already loaded user.");
    ActiveCollegeUser collegeUser = new ActiveCollegeUser(userId);
    this.activeCollegeUserMap.put(userId, collegeUser);

    NetworkUserManager networkUserManager = GlobalGateway.getNetworkUserManager();
    CollegeProfileManager collegeProfileManager = GlobalGateway.getCollegeProfileManager();
    CollegeProfileMetaDataManager metaDataManager = CollegeCore.getCollegeProfileMetaDataManager();

    List<ProfileId> profileIdsToLoad = new ArrayList<>();

    networkUserManager.applyToRemoteUser(userId, user -> {
      user.setLastSeenMinecraftName(name);
      user.setMinecraftUid(userId);
      user.setLastLoginTimestamp(System.currentTimeMillis());
      profileIdsToLoad.addAll(user.getCollegeProfiles());
    });

    networkUserManager.cache(userId);
    profileIdsToLoad.forEach(collegeProfileManager::load);
    profileIdsToLoad.forEach(metaDataManager::loadMetaData);
  }

  public void unloadData(UUID userId) {
    ActiveCollegeUser activeCollegeUser = this.activeCollegeUserMap.remove(userId);
    Preconditions.checkState(activeCollegeUser != null, "Tried unloading missing data.");

    CollegeProfileManager collegeProfileManager = GlobalGateway.getCollegeProfileManager();
    NetworkUserManager networkUserManager = GlobalGateway.getNetworkUserManager();
    CollegeProfileMetaDataManager metaDataManager = CollegeCore.getCollegeProfileMetaDataManager();

    TaskManager.runOnIOPool(() -> {
      NetworkUserData networkUserData = networkUserManager.getLocalCopy(userId);
      networkUserData.getCollegeProfiles().forEach(collegeProfileManager::unload);
      networkUserData.getCollegeProfiles().forEach(metaDataManager::unloadMetaData);
      networkUserManager.uncache(userId);
    });
  }

  public ActiveCollegeUser get(UUID userId) {
    return this.activeCollegeUserMap.get(userId);
  }

}
