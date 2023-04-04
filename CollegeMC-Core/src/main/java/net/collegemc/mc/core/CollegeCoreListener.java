package net.collegemc.mc.core;

import net.collegemc.common.GlobalGateway;
import net.collegemc.common.mineskin.data.Skin;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.core.active.ActiveCollegeUserManager;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.skinclient.DebugSkin;
import net.collegemc.mc.libs.spigot.NameGenerator;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.List;

public class CollegeCoreListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPreLogin(AsyncPlayerPreLoginEvent event) {
    if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
      return;
    }
    ActiveCollegeUserManager userManager = CollegeCore.getActiveCollegeUserManager();
    userManager.loadData(event.getUniqueId(), event.getName());
  }

  @EventHandler
  public void onLocation(PlayerSpawnLocationEvent event) {
    ActiveCollegeUser.of(event.getPlayer()).getCurrentMetaData().ifPresent(data -> {
      if (data.getLastKnownLocation() == null) {
        new IllegalStateException("Last known location is null!").printStackTrace();
      } else {
        event.setSpawnLocation(data.getLastKnownLocation());
      }
    });
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    ActiveCollegeUser collegeUser = ActiveCollegeUser.of(player);
    collegeUser.getCurrentMetaData().ifPresent(data -> {
      event.getPlayer().getInventory().setContents(data.getInventoryContent());
    });
    collegeUser.applyProfileSkin();
    TaskManager.runTask(collegeUser::applyProfileName);
    // TODO: Remove this
    if (collegeUser.getProfileList().size() < 3) {
      List<Skin> skinList = DebugSkin.select(3);
      for (int i = 0; i < 3; i++) {
        Skin skin = skinList.get(i);
        TaskManager.supplyOnIOPool(() -> {
          String name;
          NameGenerator generator = CollegeLibrary.getNameGenerator();
          do {
            name = generator.generate();
          } while (GlobalGateway.getCollegeProfileManager().nameExists(name));
          return name;
        }).thenAccept(name -> {
          collegeUser.createProfile(name, skin.getName()).join();
        }).join();
      }
    }
    Msg.sendAdminInfo(event.getPlayer(), "You have {} college profiles.", collegeUser.getProfileList().size());
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    ActiveCollegeUserManager userManager = CollegeCore.getActiveCollegeUserManager();
    userManager.unloadData(event.getPlayer().getUniqueId());
  }

}
