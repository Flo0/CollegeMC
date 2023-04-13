package net.collegemc.mc.core.friends;

import net.collegemc.common.GlobalGateway;
import net.collegemc.common.mineskin.data.Skin;
import net.collegemc.common.network.data.college.CollegeProfile;
import net.collegemc.common.network.data.college.ProfileId;
import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.core.util.OnlineProfileSelectionGUI;
import net.collegemc.mc.libs.gui.abstraction.GuiButton;
import net.collegemc.mc.libs.gui.baseimpl.PaginatedDynamicGUI;
import net.collegemc.mc.libs.gui.util.ConfirmationGUI;
import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.spigot.ItemBuilder;
import net.collegemc.mc.libs.spigot.UtilItem;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class FriendsMainGUI extends PaginatedDynamicGUI {

  private final Player player;
  private final FriendsManager friendsManager;

  public FriendsMainGUI(Player player) {
    super(3 * 9);
    this.player = player;
    this.friendsManager = CollegeCore.getFriendsManager();
  }

  @Override
  protected void setupContentList() {
    ActiveCollegeUser user = ActiveCollegeUser.of(player);
    FriendsList friendsList = user.getFriendList();
    if (friendsList != null) {
      friendsList.forEach(friendId -> this.contentList.add(createFriendButton(friendId)));
    }
  }

  @Override
  protected void setupButtons() {
    super.setupButtons();
    setButton(40, createAddButton());
    if (hasPreviousPage()) {
      setButton(36, createPreviousButton());
    }
    if (hasNextPage()) {
      setButton(44, createNextButton());
    }
  }

  private GuiButton createFriendButton(ProfileId friendId) {
    return GuiButton.builder()
            .iconCreator(() -> {
              CollegeProfile profile = GlobalGateway.getCollegeProfileManager().requestProfile(friendId);
              Skin skin = profile.getSkin();
              return ItemBuilder.of(UtilItem.produceHead(skin))
                      .name("§6" + profile.getName())
                      .lore("")
                      .lore("§cClick to remove friend")
                      .build();
            }).eventConsumer(event -> new ConfirmationGUI(result -> {
              if (result) {
                ActiveCollegeUser.of(player).getCurrentCollegeProfile()
                        .map(CollegeProfile::getCollegeProfileId)
                        .ifPresent(id -> friendsManager.removeFriend(id, friendId).thenRun(() -> {
                          CollegeProfile profile = GlobalGateway.getCollegeProfileManager().requestProfile(friendId);
                          Msg.sendInfo(player, "You have removed {} from your friend list.", profile.getName());
                        }));
              }
              new FriendsMainGUI(player).openFor(player);
            }, "§cRemove this friend?").openFor(player))
            .asyncCreated(true)
            .build();
  }

  private GuiButton createAddButton() {
    ActiveCollegeUser collegeUser = ActiveCollegeUser.of(player);
    CollegeProfile profile = collegeUser.getCurrentCollegeProfile().orElseThrow();
    FriendsList friendsList = collegeUser.getFriendList();
    assert friendsList != null;
    return GuiButton.builder()
            .iconCreator(() -> new ItemBuilder(Material.CRAFTING_TABLE)
                    .name(Component.text("§eAdd Friend"))
                    .build())
            .eventConsumer(event -> {
              new OnlineProfileSelectionGUI(selected -> {
                friendsManager.sendFriendRequest(profile.getCollegeProfileId(), selected.getCollegeProfileId()).thenRun(() -> {
                  Msg.sendInfo(event.getWhoClicked(), "You have sent a friend request to {}.", selected.getName());
                });
                new FriendsMainGUI(player).openFor(player);
              }, any -> {
                if (profile.equals(any)) {
                  return false;
                }
                return !friendsList.isFriends(any.getCollegeProfileId());
              }, "Send a friend request").openFor(player);
            })
            .build();
  }

  private GuiButton createNextButton() {
    return GuiButton.builder()
            .eventConsumer(event -> {
              if (this.hasNextPage()) {
                this.nextPage();
                UtilPlayer.playUIClick((Player) event.getWhoClicked());
              }
            }).iconCreator(() -> new ItemBuilder(Material.STICK)
                    .name(Component.text("§eNext Page"))
                    .build())
            .build();
  }

  private GuiButton createPreviousButton() {
    return GuiButton.builder()
            .eventConsumer(event -> {
              if (this.hasPreviousPage()) {
                this.previousPage();
                UtilPlayer.playUIClick((Player) event.getWhoClicked());
              }
            }).iconCreator(() -> new ItemBuilder(Material.STICK)
                    .name(Component.text("§ePrevious Page"))
                    .build())
            .build();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 5 * 9, Component.text("Friends"));
  }
}
