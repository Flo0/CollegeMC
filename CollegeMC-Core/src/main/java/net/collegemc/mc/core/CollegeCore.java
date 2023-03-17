package net.collegemc.mc.core;

import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.core.active.ActiveCollegeUserManager;
import net.collegemc.mc.core.transport.teleport.TeleportCommand;
import net.collegemc.mc.core.transport.teleport.TeleportManager;
import net.collegemc.mc.core.transport.warp.WarpCommand;
import net.collegemc.mc.core.transport.warp.WarpManager;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.CollegePlugin;
import net.collegemc.mc.libs.ServerConfigurationService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

public class CollegeCore extends CollegePlugin {

  @Getter
  private static ActiveCollegeUserManager activeCollegeUserManager;
  @Getter
  private static TeleportManager teleportManager;
  @Getter
  private static WarpManager warpManager;

  @Override
  public void onEnable() {
    activeCollegeUserManager = new ActiveCollegeUserManager();
    teleportManager = new TeleportManager();
    warpManager = new WarpManager();

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
  }

  @Override
  public void onDisable() {
    warpManager.flush();
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
