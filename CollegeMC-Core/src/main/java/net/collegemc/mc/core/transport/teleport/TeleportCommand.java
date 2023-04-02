package net.collegemc.mc.core.transport.teleport;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.libs.messaging.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermission("command.tp")
@CommandAlias("tp|teleport")
public class TeleportCommand extends BaseCommand {

  @Default
  public void onDefault(CommandSender sender) {
    Msg.sendAdminInfo(sender, "/tp here <Player>");
    Msg.sendAdminInfo(sender, "/tp to <Player>");
    Msg.sendAdminInfo(sender, "/tp to <Player> <Player>");
  }


  @Subcommand("here")
  @CommandCompletion("@ActiveCollegeUser")
  public void onTpHere(Player sender, @Values("@ActiveCollegeUser") ActiveCollegeUser target) {
    TeleportManager teleportManager = CollegeCore.getTeleportManager();
    ActiveCollegeUser senderUser = CollegeCore.getActiveCollegeUserManager().get(sender.getUniqueId());
    Player targetPlayer = target.getBukkitPlayer();

    teleportManager.teleport(targetPlayer, sender.getLocation()).thenRun(() -> {
      Msg.sendAdminInfo(sender, "You have teleported {} to you.", target.resolveName());
      Msg.sendInfo(targetPlayer, "You have been teleported to {}.", senderUser.resolveName());
    });
  }

  @Subcommand("to")
  @CommandCompletion("@ActiveCollegeUser @ActiveCollegeUser")
  public void onTpTo(Player sender, @Values("@ActiveCollegeUser") ActiveCollegeUser target, @Values("@ActiveCollegeUser") @Optional ActiveCollegeUser secondTarget) {
    TeleportManager teleportManager = CollegeCore.getTeleportManager();
    ActiveCollegeUser senderUser = CollegeCore.getActiveCollegeUserManager().get(sender.getUniqueId());

    ActiveCollegeUser movedUser = secondTarget == null ? senderUser : target;
    ActiveCollegeUser targetUser = secondTarget == null ? target : secondTarget;

    teleportManager.teleport(movedUser.getBukkitPlayer(), targetUser.getBukkitPlayer().getLocation()).thenRun(() -> {
      Msg.sendInfo(movedUser.getBukkitPlayer(), "You have been teleported to {}.", targetUser.resolveName());
    });
  }

}
