package net.collegemc.mc.core.profileselection;

import net.collegemc.common.mineskin.data.Skin;
import net.collegemc.common.network.data.college.CollegeProfile;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.hooks.itemsadder.ItemsAdderHook;
import net.collegemc.mc.libs.npcs.abstraction.NPC;
import net.collegemc.mc.libs.selectionmenu.SelectionMenu;
import net.collegemc.mc.libs.selectionmenu.baseimpl.SpectatingLockDown;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ProfileSelectionMenu extends SelectionMenu {

  private final Player player;
  private final NPC displayNpc;
  private final List<CollegeProfile> profiles;
  private int selectedIndex;

  protected ProfileSelectionMenu(Player player, ProfileSelectionLocation location) {
    super(player.getLocation(), location.getPlayerLocation(), new SpectatingLockDown(location.getPlayerLocation()), false);
    this.player = player;
    this.displayNpc = new ProfileDisplayNPC(location.getProfileLocation(), "Profile");
    CollegeLibrary.getNpcManager().add(this.displayNpc);
    this.profiles = new ArrayList<>(ActiveCollegeUser.of(player).getProfileList());
  }

  @Override
  public CompletableFuture<Void> preStart() {
    ItemsAdderHook.blackFade(this.player, 30, 30, 30);
    TaskManager.runTask(this::setup);
    return TaskManager.tickDelayedFuture(45);
  }

  @Override
  public CompletableFuture<Void> preEnd() {
    TaskManager.runTask(this::tearDown);
    ItemsAdderHook.blackFade(this.player, 30, 30, 30);
    TaskManager.runTaskLater(() -> {
      ActiveCollegeUser activeCollegeUser = ActiveCollegeUser.of(this.player);
      activeCollegeUser.switchCollegeProfile(this.getSelectedProfile().getCollegeProfileId());
    }, 30);
    return TaskManager.tickDelayedFuture(45);
  }

  private CollegeProfile getSelectedProfile() {
    return this.profiles.get(this.selectedIndex);
  }

  private void setup() {
    this.displayNpc.showTo(this.player);
    ActiveCollegeUser activeCollegeUser = ActiveCollegeUser.of(this.player);
    Optional<CollegeProfile> optActiveProfile = activeCollegeUser.getCurrentCollegeProfile();
    optActiveProfile.ifPresent(profile -> {
      this.selectedIndex = this.profiles.indexOf(profile);
      this.applyProfileOnNpc();
    });
  }

  private void applyProfileOnNpc() {
    CollegeProfile selectedProfile = this.getSelectedProfile();
    this.displayNpc.rename(selectedProfile.getName());
    Skin skin = CollegeLibrary.getPlayerSkinManager().getSkin(selectedProfile.getSkinName());
    if (skin != null) {
      this.displayNpc.setSkin(skin);
    }
    this.displayNpc.broadcastNameChange();
    this.displayNpc.broadcastSkinUpdate();
  }

  private void tearDown() {
    this.displayNpc.broadcastHide();
    CollegeLibrary.getNpcManager().removeById(this.displayNpc.getEntityId());
  }

  public void changeSelection(CollegeProfile profile) {
    this.selectedIndex = this.profiles.indexOf(profile);
    this.applyProfileOnNpc();
  }

  @Override
  protected void swingSelect() {
    new ProfileSelectionGUI(this.player, this::changeSelection).openFor(this.player);
    UtilPlayer.playUIClick(this.player);
  }
}