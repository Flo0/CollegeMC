package net.collegemc.mc.libs.npcs;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.npcs.abstraction.NPC;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandPermission("command.npc")
@CommandAlias("npc")
public class NPCCommand extends BaseCommand {

  @Default
  public void onDefault(CommandSender sender) {
    Msg.sendAdminInfo(sender, "/npc create <NPC> <DisplayName>");
    Msg.sendAdminInfo(sender, "/npc skin url <NPC> <URL>");
    Msg.sendAdminInfo(sender, "/npc rename <NPC> <Name>");
  }

  @Subcommand("rename")
  @CommandCompletion("@NPC @nothing")
  public void onRename(Player sender, String internalName, String displayName) {
    NPC npc = CollegeLibrary.getNpcManager().getByName(internalName);
    if (npc == null) {
      Msg.sendError(sender, "No NPC with the name {} exists.", internalName);
      return;
    }
    npc.rename(displayName);
    npc.broadcastNameChange();
    Msg.sendAdminInfo(sender, "You have changed the name of {} to {}.", internalName, displayName);
  }

  @Subcommand("debug")
  public void debugName(Player player, String text) {

  }

  @Subcommand("create")
  public void onCreate(Player sender, String internalName, String displayName) {
    NPCManager npcManager = CollegeLibrary.getNpcManager();

    if (npcManager.getByName(internalName) != null) {
      Msg.sendError(sender, "An NPC with the internal name {} already exists.", internalName);
      return;
    }

    Location location = sender.getLocation();
    location.setDirection(sender.getEyeLocation().getDirection());

    IdleNPC npc = new IdleNPC(location, internalName, displayName);
    npcManager.add(npc);
    npc.broadcastShow();

    Msg.sendAdminInfo(sender, "You created the NPC {}.", internalName);
    Msg.sendAdminInfo(sender, "Use one of those commands to set his skin:");
    Msg.sendAdminInfo(sender, "- {}", "/npc skin url <NPC> <URL>");
    Msg.sendAdminInfo(sender, "- {}", "/npc skin name <NPC> <Name>");
  }

}
