package net.collegemc.mc.libs.regions;

import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Preconditions;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.regions.permissions.RegionPermission;
import net.collegemc.mc.libs.tasks.MongoBackedMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.Flushable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RegionManager implements Iterable<AbstractRegion>, Flushable {

  public static final String NAMESPACE = "Regions";

  private final Map<UUID, RegionSelection> playerRegionSelections = new HashMap<>();
  private final Map<UUID, RegionWorldDomain> worldDomainMap = new HashMap<>();
  private final MongoBackedMap<String, AbstractRegion> regionNameMap;

  public RegionManager(JavaPlugin plugin) {
    this.regionNameMap = new MongoBackedMap<>(new HashMap<>(), NAMESPACE, String.class, AbstractRegion.class);
    this.regionNameMap.loadDataFromRemote();

    Bukkit.getPluginManager().registerEvents(new RegionListener(), plugin);
    PaperCommandManager commandManager = CollegeLibrary.getCommandManager();
    commandManager.getCommandContexts().registerContext(AbstractRegion.class, context -> this.regionNameMap.get(context.popFirstArg()));
    commandManager.getCommandCompletions().registerCompletion("Regions", context -> {
      return List.copyOf(this.regionNameMap.keySet());
    });
    commandManager.getCommandCompletions().registerStaticCompletion("RegionPermissions", () -> {
      return Arrays.stream(RegionPermission.values()).map(Enum::toString).toList();
    });
    commandManager.registerCommand(new RegionCommand());
  }

  public void addRegion(AbstractRegion region) {
    Preconditions.checkArgument(!this.regionNameMap.containsKey(region.getName()));
    this.worldDomainMap.computeIfAbsent(region.getWorldId(), key -> new RegionWorldDomain()).addRegion(region);
    this.regionNameMap.put(region.getName(), region);
  }

  public AbstractRegion getRegionWithHighestPriorityAt(Location location) {
    World world = location.getWorld();
    Preconditions.checkArgument(world != null);
    RegionWorldDomain domain = this.worldDomainMap.computeIfAbsent(world.getUID(), key -> new RegionWorldDomain());
    return domain.getRegionWithHighestPriorityAt(location);
  }

  public AbstractRegion getRegionByName(String regionName) {
    return this.regionNameMap.get(regionName);
  }

  public void removeRegion(AbstractRegion region) {
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
  public Iterator<AbstractRegion> iterator() {
    return this.regionNameMap.values().iterator();
  }

  @Override
  public void flush() {
    this.regionNameMap.saveDataToRemote();
  }
}
