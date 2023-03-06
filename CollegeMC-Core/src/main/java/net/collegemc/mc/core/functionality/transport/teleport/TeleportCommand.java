package net.collegemc.mc.core.functionality.transport.teleport;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import net.collegemc.mc.core.functionality.CollegeCore;
import net.collegemc.mc.core.functionality.active.ActiveCollegeUser;
import net.collegemc.mc.libs.messaging.Msg;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermission("ADMIN")
@CommandAlias("tp|teleport")
public class TeleportCommand extends BaseCommand {

  @Default
  public void onDefault(CommandSender sender) {
    Msg.sendAdminInfo(sender, "/tp here <Player>");
    Msg.sendAdminInfo(sender, "/tp to <Player>");
    Msg.sendAdminInfo(sender, "/tp to <Player> <Player>");
    Msg.sendAdminInfo(sender, "/tp at <Player> <x> <y> <z>");
  }

  @Subcommand("here")
  public void onTpHere(Player sender, ActiveCollegeUser target) {
    TeleportManager teleportManager = CollegeCore.getTeleportManager();
    ActiveCollegeUser senderUser = CollegeCore.getActiveCollegeUserManager().get(sender.getUniqueId());
    Player targetPlayer = target.getBukkitPlayer();

    teleportManager.teleport(targetPlayer, sender.getLocation()).thenRun(() -> {
      Msg.sendAdminInfo(sender, "You have teleported {} to you.", target.resolveName());
      Msg.sendInfo(targetPlayer, "You have been teleported to {}.", senderUser.resolveName());
    });
  }

  @Subcommand("to")
  public void onTpTo(Player sender, ActiveCollegeUser target, @Optional ActiveCollegeUser secondTarget) {
    TeleportManager teleportManager = CollegeCore.getTeleportManager();
    ActiveCollegeUser senderUser = CollegeCore.getActiveCollegeUserManager().get(sender.getUniqueId());

    ActiveCollegeUser movedUser = secondTarget == null ? senderUser : target;
    ActiveCollegeUser targetUser = secondTarget == null ? target : secondTarget;

    teleportManager.teleport(movedUser.getBukkitPlayer(), targetUser.getBukkitPlayer().getLocation()).thenRun(() -> {
      Msg.sendInfo(movedUser.getBukkitPlayer(), "You have been teleported to {}.", targetUser.resolveName());
    });
  }

  @Subcommand("at")
  public void onTpAt(Player sender, OnlinePlayer target, Location location) {
    TeleportManager teleportManager = CollegeCore.getTeleportManager();
    teleportManager.teleport(target.player, location);
  }

}
