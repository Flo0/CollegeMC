package net.collegemc.mc.libs.regions;

import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Preconditions;
import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RegionManager implements Iterable<Region> {

  private final Map<UUID, RegionWorldDomain> worldDomainMap = new HashMap<>();
  private final Map<String, Region> regionNameMap = new HashMap<>();
  private final Map<UUID, RegionSelection> playerRegionSelections = new HashMap<>();

  public RegionManager(JavaPlugin plugin) {
    Bukkit.getPluginManager().registerEvents(new RegionListener(), plugin);
    PaperCommandManager commandManager = CollegeLibrary.getCommandManager();
    commandManager.getCommandCompletions().registerCompletion("Regions", context -> {
      return List.copyOf(this.regionNameMap.keySet());
    });
    commandManager.registerCommand(new RegionCommand());
  }

  public void addRegion(Region region) {
    Preconditions.checkArgument(!this.regionNameMap.containsKey(region.getName()));
    this.worldDomainMap.computeIfAbsent(region.getWorldId(), key -> new RegionWorldDomain()).addRegion(region);
    this.regionNameMap.put(region.getName(), region);
  }

  public Region getRegionWithHighestPriorityAt(Location location) {
    World world = location.getWorld();
    Preconditions.checkArgument(world != null);
    RegionWorldDomain domain = this.worldDomainMap.computeIfAbsent(world.getUID(), key -> new RegionWorldDomain());
    return domain.getRegionWithHighestPriorityAt(location);
  }

  public Region getRegionByName(String regionName) {
    return this.regionNameMap.get(regionName);
  }

  public void removeRegion(Region region) {
    this.worldDomainMap.computeIfAbsent(region.getWorldId(), key -> new RegionWorldDomain()).removeRegion(region);
    this.regionNameMap.remove(region.getName());
  }

  public RegionSelection getSelection(Player player) {
    return this.playerRegionSelections.computeIfAbsent(player.getUniqueId(), key -> new RegionSelection());
  }

  public void removeSelection(Player player) {
    this.playerRegionSelections.remove(player.getUniqueId());
  }

  @NotNull
  @Override
  public Iterator<Region> iterator() {
    return this.regionNameMap.values().iterator();
  }
}
