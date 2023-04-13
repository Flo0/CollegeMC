package net.collegemc.mc.core.profileselection;

import net.collegemc.common.mineskin.data.Skin;
import net.collegemc.common.network.data.college.CollegeProfile;
import net.collegemc.mc.core.active.ActiveCollegeUser;
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

  private final ProfileSelectionLocation location;
  private final Player player;
  private final NPC displayNpc;
  private final List<CollegeProfile> profiles;
  private int selectedIndex;

  protected ProfileSelectionMenu(Player player, ProfileSelectionLocation location) {
    super(new SpectatingLockDown(location.getPlayerLocation()));
    this.location = location;
    this.player = player;
    this.displayNpc = new ProfileDisplayNPC(location.getProfileLocation(), "Profile");
    this.profiles = new ArrayList<>(ActiveCollegeUser.of(player).getProfileList());
  }

  @Override
  public CompletableFuture<Void> onStart() {
    ItemsAdderHook.blackFade(this.player, 30, 30, 30);
    this.setup();
    CompletableFuture<Void> teleportFuture = new CompletableFuture<>();
    TaskManager.runTaskLater(() -> player.teleportAsync(this.location.getPlayerLocation()).thenRun(() -> teleportFuture.complete(null)), 40);
    return teleportFuture;
  }

  @Override
  public CompletableFuture<Void> onEnd(boolean now) {
    TaskManager.runTask(this::tearDown);
    ItemsAdderHook.blackFade(this.player, 30, 30, 30);
    TaskManager.runTaskLater(() -> {
      ActiveCollegeUser activeCollegeUser = ActiveCollegeUser.of(this.player);
      activeCollegeUser.switchCollegeProfile(this.getSelectedProfile().getCollegeProfileId());
    }, 40);
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
    Skin skin = selectedProfile.getSkin();
    if (skin != null) {
      this.displayNpc.setSkin(skin);
    }
    this.displayNpc.broadcastSkinUpdate();
    this.displayNpc.broadcastNameChange();
  }

  private void tearDown() {
    this.displayNpc.broadcastHide();
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

  @Override
  protected boolean isTicked() {
    return true;
  }

  @Override
  protected void onTick() {
    displayNpc.onTick();
  }
}