package net.collegemc.mc.libs.regions;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.messaging.Msg;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@CommandPermission("command.region")
@CommandAlias("region|rg")
public class RegionCommand extends BaseCommand {

  @Default
  public void onDefault(CommandSender sender) {
    Msg.sendAdminInfo(sender, "/region pos1");
    Msg.sendAdminInfo(sender, "/region pos2");
    Msg.sendAdminInfo(sender, "/region create <Name>");
    Msg.sendAdminInfo(sender, "/region tp <Name>");
  }

  @Subcommand("pos1")
  public void onPos1(Player player) {
    RegionSelection selection = CollegeLibrary.getRegionManager().getSelection(player);
    selection.setFirst(player.getLocation());
    Msg.sendAdminInfo(player, "You have selected the {} position.", "first");
  }

  @Subcommand("pos2")
  public void onPos2(Player player) {
    RegionSelection selection = CollegeLibrary.getRegionManager().getSelection(player);
    selection.setSecond(player.getLocation());
    Msg.sendAdminInfo(player, "You have selected the {} position.", "second");
  }

  @Subcommand("tp")
  @CommandCompletion("@Regions")
  public void onTp(Player player, String regionName) {
    Region region = CollegeLibrary.getRegionManager().getRegionByName(regionName);
    if (region == null) {
      Msg.sendError(player, "No region with the name {} found.", regionName);
      return;
    }
    BoundingBox boundingBox = region.getBoundingBox();
    Vector center = boundingBox.getCenter();
    float yaw = player.getEyeLocation().getYaw();
    float pitch = player.getEyeLocation().getPitch();
    Location tpLoc = center.toLocation(player.getWorld(), yaw, pitch);
    player.teleportAsync(tpLoc).thenRun(() -> Msg.sendAdminInfo(player, "You have been teleported to {}.", regionName));
  }

  @Subcommand("create")
  public void onCreate(Player player, String regionName, @Default("1") Integer priority) {
    RegionManager regionManager = CollegeLibrary.getRegionManager();
    RegionSelection selection = regionManager.getSelection(player);
    if (!selection.valid()) {
      Msg.sendError(player, "You need to select two points first.");
      return;
    }
    if (regionManager.getRegionByName(regionName) != null) {
      Msg.sendError(player, "A region with the name {} already exists.", regionName);
      return;
    }
    int prio = priority == null ? 1 : priority;
    Block first = selection.getFirst().getBlock();
    Block second = selection.getSecond().getBlock();
    BoundingBox boundingBox = BoundingBox.of(first, second);
    Region region = new Region(prio, regionName, boundingBox, player.getWorld().getUID());
    regionManager.addRegion(region);
    Msg.sendAdminInfo(player, "You created the {} region.", regionName);
  }

  @Subcommand("remove")
  @CommandCompletion("@Regions")
  public void onRemove(Player player, String regionName) {
    RegionManager regionManager = CollegeLibrary.getRegionManager();
    Region region = regionManager.getRegionByName(regionName);
    if (region == null) {
      Msg.sendError(player, "No region with the name {} found.", regionName);
      return;
    }
    regionManager.removeRegion(region);
    Msg.sendAdminInfo(player, "The region {} was removed.", regionName);
  }

}
