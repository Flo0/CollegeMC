package net.collegemc.mc.libs;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CollegePlugin extends JavaPlugin {

  @Override
  public void onLoad() {
    this.getLogger().info("Registering configuration service...");
    Bukkit.getServicesManager().register(ServerConfigurationService.class, this.provideConfig(), this, this.getPriority());
  }

  public abstract ServerConfigurationService provideConfig();

  public ServicePriority getPriority() {
    return ServicePriority.Normal;
  }

}
