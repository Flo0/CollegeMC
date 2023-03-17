package net.collegemc.mc.libs.regions;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.regions.impl.BoxRegion;
import net.collegemc.mc.libs.regions.impl.PolygonRegion;
import net.collegemc.mc.libs.regions.impl.Vec2D;
import net.collegemc.mc.libs.regions.permissions.RegionPermission;
import net.collegemc.mc.libs.regions.permissions.RegionPermissionGUI;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;

@CommandPermission("command.region")
@CommandAlias("region|rg")
public class RegionCommand extends BaseCommand {

  @Default
  public void onDefault(CommandSender sender) {
    Msg.sendAdminInfo(sender, "/region box corner1");
    Msg.sendAdminInfo(sender, "/region box corner2");
    Msg.sendAdminInfo(sender, "/region box create <Name>");
    Msg.sendAdminInfo(sender, "/region poly create <Name>");
    Msg.sendAdminInfo(sender, "/region poly addcorner");
    Msg.sendAdminInfo(sender, "/region poly clear");
    Msg.sendAdminInfo(sender, "/region tp <Name>");
    Msg.sendAdminInfo(sender, "/region remove <Name>");
    Msg.sendAdminInfo(sender, "/region permission add <Region> <Player> <Permission>");
    Msg.sendAdminInfo(sender, "/region permission remove <Region> <Player> <Permission>");
    Msg.sendAdminInfo(sender, "/region permissions <Region>");
  }

  @Subcommand("permission add")
  @CommandCompletion("@Regions @Players @RegionPermissions")
  public void onRegionPermissionAdd(Player player, AbstractRegion region, OfflinePlayer target, RegionPermission permission) {
    region.getPermissionContainer().addPermission(target.getUniqueId(), permission);
    Msg.sendAdminInfo(player, "You have added the {} permission of {} for the {} region.", permission.getDisplayName(), target.getName(), region.getName());
  }

  @Subcommand("permission remove")
  @CommandCompletion("@Regions @Players @RegionPermissions")
  public void onRegionPermissionRemove(Player player, AbstractRegion region, OfflinePlayer target, RegionPermission permission) {
    region.getPermissionContainer().removePermission(target.getUniqueId(), permission);
    Msg.sendAdminInfo(player, "You have removed the {} permission of {} for the {} region.", permission.getDisplayName(), target.getName(), region.getName());
  }

  @Subcommand("permission list")
  @CommandCompletion("@Regions")
  public void onRegionPermissions(Player player, AbstractRegion region) {
    new RegionPermissionGUI(region).openFor(player);
    Msg.sendAdminInfo(player, "You are now viewing the permissions of {}.", region.getName());
  }

  @Subcommand("poly addcorner")
  public void onPolyAddCorner(Player player) {
    RegionSelection selection = CollegeLibrary.getRegionManager().getSelection(player);
    selection.addPolyCorner(player.getLocation());
    Msg.sendAdminInfo(player, "You have added the {}. corner for your polygon.", selection.getPolySelections().size());
  }

  @Subcommand("poly clear")
  public void onPolyClear(Player player) {
    RegionSelection selection = CollegeLibrary.getRegionManager().getSelection(player);
    selection.clearPolyCorners();
    Msg.sendAdminInfo(player, "You have {} all corners for your polygon.", "cleared");
  }

  @Subcommand("poly create")
  public void onCreatePoly(Player player, String regionName, double height, @Default("1") Integer priority) {
    RegionManager regionManager = CollegeLibrary.getRegionManager();
    RegionSelection selection = regionManager.getSelection(player);
    if (!selection.hasEnoughPolyCorners()) {
      Msg.sendError(player, "You need to select at least {} polygon corners.", 3);
      return;
    }
    if (regionManager.getRegionByName(regionName) != null) {
      Msg.sendError(player, "A region with the name {} already exists.", regionName);
      return;
    }
    int prio = priority == null ? 1 : priority;
    List<Vec2D> corners = selection.getPolySelectionProjected2D();
    double base = player.getLocation().getY();
    AbstractRegion region = new PolygonRegion(prio, regionName, player.getWorld().getUID(), corners, base, height);
    regionManager.addRegion(region);
    Msg.sendAdminInfo(player, "You created the {} polygon region.", regionName);
  }

  @Subcommand("box corner1")
  public void onPos1(Player player) {
    RegionSelection selection = CollegeLibrary.getRegionManager().getSelection(player);
    selection.setFirst(player.getLocation());
    Msg.sendAdminInfo(player, "You have selected the {} position.", "first");
  }

  @Subcommand("box corner2")
  public void onPos2(Player player) {
    RegionSelection selection = CollegeLibrary.getRegionManager().getSelection(player);
    selection.setSecond(player.getLocation());
    Msg.sendAdminInfo(player, "You have selected the {} position.", "second");
  }

  @Subcommand("tp")
  @CommandCompletion("@Regions")
  public void onTp(Player player, String regionName) {
    AbstractRegion region = CollegeLibrary.getRegionManager().getRegionByName(regionName);
    if (region == null) {
      Msg.sendError(player, "No region with the name {} found.", regionName);
      return;
    }
    Vector center = region.getCenter();
    float yaw = player.getEyeLocation().getYaw();
    float pitch = player.getEyeLocation().getPitch();
    Location tpLoc = center.toLocation(player.getWorld(), yaw, pitch);
    player.teleportAsync(tpLoc).thenRun(() -> Msg.sendAdminInfo(player, "You have been teleported to {}.", regionName));
  }

  @Subcommand("box create")
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
    AbstractRegion region = new BoxRegion(prio, regionName, boundingBox, player.getWorld().getUID());
    regionManager.addRegion(region);
    Msg.sendAdminInfo(player, "You created the {} box region.", regionName);
  }

  @Subcommand("remove")
  @CommandCompletion("@Regions")
  public void onRemove(Player player, String regionName) {
    RegionManager regionManager = CollegeLibrary.getRegionManager();
    AbstractRegion region = regionManager.getRegionByName(regionName);
    if (region == null) {
      Msg.sendError(player, "No region with the name {} found.", regionName);
      return;
    }
    regionManager.removeRegion(region);
    Msg.sendAdminInfo(player, "The region {} was removed.", regionName);
  }

}
