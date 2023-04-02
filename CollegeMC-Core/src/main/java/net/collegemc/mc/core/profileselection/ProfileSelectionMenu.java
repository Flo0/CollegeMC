package net.collegemc.mc.core.profileselection;

import net.collegemc.common.GlobalGateway;
import net.collegemc.common.network.data.college.CollegeProfile;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.hooks.itemsadder.ItemsAdderHook;
import net.collegemc.mc.libs.npcs.abstraction.NPC;
import net.collegemc.mc.libs.selectionmenu.SelectionMenu;
import net.collegemc.mc.libs.selectionmenu.baseimpl.SlowLockDown;
import net.collegemc.mc.libs.spigot.NameGenerator;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.entity.Player;
import org.mineskin.data.Skin;

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
    super(player.getLocation(), location.getPlayerLocation(), new SlowLockDown(), false);
    this.player = player;
    this.displayNpc = new ProfileDisplayNPC(location.getProfileLocation(), "Profile");
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
    return TaskManager.tickDelayedFuture(45);
  }

  @Override
  protected boolean isTicked() {
    return true;
  }

  @Override
  protected void onTick() {
    this.displayNpc.onTick();
  }

  private CollegeProfile getSelectedProfile() {
    return this.profiles.get(this.selectedIndex);
  }

  private void setup() {
    this.displayNpc.showTo(this.player);
    ActiveCollegeUser activeCollegeUser = ActiveCollegeUser.of(this.player);
    Optional<CollegeProfile> optActiveProfile = activeCollegeUser.getCurrentCollegeProfile();
    if (optActiveProfile.isEmpty()) {
      TaskManager.supplyOnIOPool(() -> {
        String name;
        NameGenerator generator = CollegeLibrary.getNameGenerator();
        do {
          name = generator.generate();
        } while (GlobalGateway.getCollegeProfileManager().nameExists(name));
        return name;
      }).thenAccept(name -> {
        CollegeProfile collegeProfile = activeCollegeUser.createProfile(name).join();
        TaskManager.runTask(() -> {
          this.profiles.add(collegeProfile);
          this.applyProfileOnNpc();
        });
      });
    } else {
      this.selectedIndex = this.profiles.indexOf(optActiveProfile.get());
      this.applyProfileOnNpc();
    }
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
    ActiveCollegeUser activeCollegeUser = ActiveCollegeUser.of(this.player);
    this.displayNpc.hideFrom(this.player);
    activeCollegeUser.switchCollegeProfile(this.getSelectedProfile().getCollegeProfileId());
  }

  @Override
  protected void swingSelect() {
    UtilPlayer.playUIClick(this.player);
  }
}