package net.collegemc.mc.core.friends;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.network.data.college.CollegeProfile;
import net.collegemc.common.network.data.college.CollegeProfileManager;
import net.collegemc.common.network.data.college.ProfileId;
import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CommandAlias("friends")
public class FriendsCommand extends BaseCommand {

  @Default
  public void onDefault(Player sender) {

  }

  @Subcommand("request")
  @CommandCompletion("@ActiveCollegeUser")
  public void onAdd(Player sender, @Values("@ActiveCollegeUser") ActiveCollegeUser target) {
    FriendsManager friendsManager = CollegeCore.getFriendsManager();
    ActiveCollegeUser senderUser = ActiveCollegeUser.of(sender);

    Optional<CollegeProfile> optSenderProfile = senderUser.getCurrentCollegeProfile();
    if (optSenderProfile.isEmpty()) {
      Msg.sendError(sender, "You dont have an active profile.");
      UtilPlayer.playFailurePling(sender);
      return;
    }

    FriendsList friendsList = friendsManager.getActiveFriendsList(optSenderProfile.get().getCollegeProfileId());

    Optional<CollegeProfile> optTargetProfile = target.getCurrentCollegeProfile();
    if (optTargetProfile.isEmpty()) {
      Msg.sendError(sender, "The target has no active profile.");
      UtilPlayer.playFailurePling(sender);
      return;
    }

    ProfileId senderId = optSenderProfile.get().getCollegeProfileId();
    ProfileId targetId = optTargetProfile.get().getCollegeProfileId();

    if (senderId.equals(targetId)) {
      Msg.sendError(sender, "You cant add yourself as a friend.");
      UtilPlayer.playFailurePling(sender);
      return;
    }

    if (friendsList.isFriends(targetId)) {
      Msg.sendInfo(sender, "You are already friends with {}.", optTargetProfile.get().getName());
      UtilPlayer.playFailurePling(sender);
      return;
    }

    friendsManager.hasSentFriendRequest(senderId, targetId).thenAccept(value -> {
      if (value) {
        Msg.sendError(sender, "You have already sent a request to {}.", optTargetProfile.get().getName());
        UtilPlayer.playFailurePling(sender);
      } else {
        friendsManager.sendFriendRequest(senderId, targetId).thenRun(() -> {
          Player targetPlayer = target.getBukkitPlayer();
          Msg.sendInfo(sender, "You have sent a request to {}.", optTargetProfile.get().getName());
          Msg.sendInfo(targetPlayer, "You have received a friend request from {}.", optSenderProfile.get().getName());
          UtilPlayer.playSuccessPling(sender);
          UtilPlayer.playSuccessPling(targetPlayer);
        });
      }
    });
  }

  @Subcommand("remove")
  @CommandCompletion("@Friends")
  public void onRemove(Player sender, @Values("@Friends") String name) {
    CollegeProfileManager profileManager = GlobalGateway.getCollegeProfileManager();
    ActiveCollegeUser senderUser = ActiveCollegeUser.of(sender);
    Optional<CollegeProfile> optSenderProfile = senderUser.getCurrentCollegeProfile();

    if (optSenderProfile.isEmpty()) {
      Msg.sendError(sender, "You dont have an active profile.");
      UtilPlayer.playFailurePling(sender);
      return;
    }

    FriendsManager friendsManager = CollegeCore.getFriendsManager();
    FriendsList friendsList = friendsManager.getActiveFriendsList(optSenderProfile.get().getCollegeProfileId());

    TaskManager.runOnIOPool(() -> {
      ProfileId targetProfileId = profileManager.getIdByName(name);
      if (!friendsList.isFriends(targetProfileId)) {
        Msg.sendError(sender, "You are not friends with {}.", name);
        UtilPlayer.playFailurePling(sender);
        return;
      }
      friendsManager.removeFriend(optSenderProfile.get().getCollegeProfileId(), targetProfileId).thenRun(() -> {
        Msg.sendInfo(sender, "You have terminated your friendship with {}.", name);
        UtilPlayer.playSuccessPling(sender);
        CollegeProfile targetProfile = profileManager.getLoaded(targetProfileId);
        if (targetProfile == null) {
          return;
        }

        UUID targetMinecraftId = targetProfile.getMinecraftUserId();
        Player targetPlayer = Bukkit.getPlayer(targetMinecraftId);
        if (targetPlayer == null) {
          return;
        }

        Msg.sendInfo(targetPlayer, "{} has terminated your friendship.", optSenderProfile.get().getName());
        UtilPlayer.playSuccessPling(targetPlayer);
      });
    });
  }

  @Subcommand("online")
  public void onOnline(Player sender) {
    FriendsManager friendsManager = CollegeCore.getFriendsManager();
    CollegeProfileManager profileManager = GlobalGateway.getCollegeProfileManager();
    ActiveCollegeUser senderUser = ActiveCollegeUser.of(sender);

    Optional<CollegeProfile> optSenderProfile = senderUser.getCurrentCollegeProfile();
    if (optSenderProfile.isEmpty()) {
      Msg.sendError(sender, "You dont have an active profile.");
      UtilPlayer.playFailurePling(sender);
      return;
    }

    FriendsList friendsList = friendsManager.getActiveFriendsList(optSenderProfile.get().getCollegeProfileId());

    if (friendsList.size() == 0) {
      Msg.sendInfo(sender, "You dont have any friends...");
      return;
    }

    List<String> names = new ArrayList<>();
    friendsList.getFriends().forEach(friendId -> {
      CollegeProfile friendProfile = profileManager.getLoaded(friendId);
      if (friendProfile != null) {
        names.add(friendProfile.getName());
      }
    });

    if (names.size() == 0) {
      Msg.sendInfo(sender, "{} of your {} friends are online.", "None", friendsList.size());
      return;
    }
    Msg.sendInfo(sender, "There are current [{}/{}] friends online", names.size(), friendsList.size());
    for (String name : names) {
      sender.sendMessage("§f- §a" + name);
    }
  }

}
