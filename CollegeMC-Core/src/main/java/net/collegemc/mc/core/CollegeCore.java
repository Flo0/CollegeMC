package net.collegemc.mc.core;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import net.collegemc.common.GlobalGateway;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.core.active.ActiveCollegeUserManager;
import net.collegemc.mc.core.active.CollegeProfileMetaDataManager;
import net.collegemc.mc.core.friends.FriendsList;
import net.collegemc.mc.core.friends.FriendsManager;
import net.collegemc.mc.core.profileselection.ProfileSelectionCommand;
import net.collegemc.mc.core.profileselection.ProfileSelectionManager;
import net.collegemc.mc.core.transport.teleport.TeleportCommand;
import net.collegemc.mc.core.transport.teleport.TeleportManager;
import net.collegemc.mc.core.transport.warp.WarpCommand;
import net.collegemc.mc.core.transport.warp.WarpManager;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.CollegePlugin;
import net.collegemc.mc.libs.ServerConfigurationService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

import java.util.List;

public class CollegeCore extends CollegePlugin {

  @Getter
  private static ActiveCollegeUserManager activeCollegeUserManager;
  @Getter
  private static TeleportManager teleportManager;
  @Getter
  private static WarpManager warpManager;
  @Getter
  private static FriendsManager friendsManager;
  @Getter
  private static CollegeProfileMetaDataManager collegeProfileMetaDataManager;
  @Getter
  private static ProfileSelectionManager profileSelectionManager;

  @Override
  public void onEnable() {
    activeCollegeUserManager = new ActiveCollegeUserManager();
    teleportManager = new TeleportManager();
    warpManager = new WarpManager();
    friendsManager = new FriendsManager();
    collegeProfileMetaDataManager = new CollegeProfileMetaDataManager();
    profileSelectionManager = new ProfileSelectionManager();

    PaperCommandManager commandManager = CollegeLibrary.getCommandManager();

    Bukkit.getPluginManager().registerEvents(new CollegeCoreListener(), this);

    commandManager.getCommandContexts().registerContext(ActiveCollegeUser.class, context -> {
      String arg = context.popFirstArg();
      return activeCollegeUserManager.getByName(arg);
    });

    commandManager.registerCommand(new TeleportCommand());
    commandManager.getCommandCompletions().registerCompletion("ActiveCollegeUser", context -> {
      return activeCollegeUserManager.getUserNames();
    });

    commandManager.registerCommand(new WarpCommand());
    commandManager.getCommandCompletions().registerCompletion("Warps", context -> {
      return warpManager.getWarpNames();
    });

    commandManager.registerCommand(new WarpCommand());
    commandManager.getCommandCompletions().registerAsyncCompletion("Friends", context -> {
      ActiveCollegeUser sender = ActiveCollegeUser.of(context.getPlayer());
      return sender.getCurrentCollegeProfile().map(profile -> {
        FriendsList friendsList = friendsManager.getActiveFriendsList(profile.getCollegeProfileId());
        return GlobalGateway.getCollegeProfileManager().getAllNames(friendsList.getFriends());
      }).orElse(List.of("NoFriends..."));
    });

    commandManager.registerCommand(new ProfileSelectionCommand());
    commandManager.getCommandCompletions().registerCompletion("ProfileSelectionLocations", context -> {
      return profileSelectionManager.getLocationNames();
    });
  }

  @Override
  public void onDisable() {
    warpManager.flush();
    profileSelectionManager.flush();
  }

  @Override
  public ServerConfigurationService provideConfig() {
    return new FallbackConfigurationService();
  }

  @Override
  public ServicePriority getPriority() {
    return ServicePriority.Lowest;
  }

}
