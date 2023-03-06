package net.collegemc.mc.core.functionality;

import lombok.Getter;
import net.collegemc.mc.core.functionality.active.ActiveCollegeUserManager;
import net.collegemc.mc.core.functionality.transport.teleport.TeleportCommand;
import net.collegemc.mc.core.functionality.transport.teleport.TeleportManager;
import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CollegeCore extends JavaPlugin {

  @Getter
  private static ActiveCollegeUserManager activeCollegeUserManager;
  @Getter
  private static TeleportManager teleportManager;

  @Override
  public void onEnable() {
    activeCollegeUserManager = new ActiveCollegeUserManager();
    teleportManager = new TeleportManager(activeCollegeUserManager);

    Bukkit.getPluginManager().registerEvents(new CollegeCoreListener(), this);
    CollegeLibrary.getCommandManager().registerCommand(new TeleportCommand());
  }

  @Override
  public void onDisable() {

  }

}
