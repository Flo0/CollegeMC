package net.collegemc.mc.core.transport.warp;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.libs.messaging.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermission("command.warp")
@CommandAlias("warp")
public class WarpCommand extends BaseCommand {

  @Default
  public void onDefault(CommandSender sender) {
    Msg.sendAdminInfo(sender, "/warp create <Warp>");
    Msg.sendAdminInfo(sender, "/warp tp <Warp>");
    Msg.sendAdminInfo(sender, "/warp tpplayer <Player> <Warp>");
    Msg.sendAdminInfo(sender, "/warp remove <Warp>");
    Msg.sendAdminInfo(sender, "/warp list");
  }

  @Subcommand("create")
  public void onWarpCreate(Player sender, String name) {
    WarpManager warpManager = CollegeCore.getWarpManager();
    Warp warp = warpManager.getWarp(name);
    if (warp != null) {
      Msg.sendError(sender, "A warp with the name {} already exists.", name);
      return;
    }
    warpManager.addWarp(new Warp(sender.getLocation(), name));
    Msg.sendAdminInfo(sender, "The warp {} was created.", name);
  }

  @Subcommand("tp")
  @CommandCompletion("@Warps")
  public void onWarpTp(Player sender, @Values("@Warps") String name) {
    WarpManager warpManager = CollegeCore.getWarpManager();
    Warp warp = warpManager.getWarp(name);
    if (warp == null) {
      Msg.sendError(sender, "No warp with the name {} exists.", name);
      return;
    }
    CollegeCore.getTeleportManager().teleport(sender, warp.getLocation()).thenRun(() -> {
      Msg.sendAdminInfo(sender, "You have been teleported to {}.", name);
    });
  }

  @Subcommand("tpplayer")
  @CommandCompletion("@ActiveCollegeUser @Warps")
  public void onWarpTp(Player sender, @Values("@ActiveCollegeUser") ActiveCollegeUser user, @Values("@Warps") String name) {
    WarpManager warpManager = CollegeCore.getWarpManager();
    Warp warp = warpManager.getWarp(name);
    if (warp == null) {
      Msg.sendError(sender, "No warp with the name {} exists.", name);
      return;
    }
    Msg.sendAdminInfo(sender, "You have teleported {} to {}.", user.resolveName(), name);
    CollegeCore.getTeleportManager().teleport(user.getBukkitPlayer(), warp.getLocation()).thenRun(() -> {
      Msg.sendInfo(user.getBukkitPlayer(), "You have been teleported to {}.", name);
    });
  }

  @Subcommand("remove")
  @CommandCompletion("@Warps")
  public void onWarpRemove(CommandSender sender, @Values("@Warps") String name) {
    WarpManager warpManager = CollegeCore.getWarpManager();
    Warp warp = warpManager.getWarp(name);
    if (warp == null) {
      Msg.sendError(sender, "No warp with the name {} exists.", name);
      return;
    }
    warpManager.removeWarp(name);
    Msg.sendAdminInfo(sender, "The warp {} has been deleted.", name);
  }

}
