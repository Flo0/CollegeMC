package net.collegemc.mc.libs;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CollegePlugin extends JavaPlugin {

  @Override
  public void onLoad() {
    Bukkit.getServicesManager().register(ServerConfigurationService.class, this.provideConfig(), this, ServicePriority.Normal);
  }

  public abstract ServerConfigurationService provideConfig();

}
