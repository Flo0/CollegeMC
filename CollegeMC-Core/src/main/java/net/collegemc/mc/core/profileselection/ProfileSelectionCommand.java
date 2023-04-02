package net.collegemc.mc.core.profileselection;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.libs.messaging.Msg;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.WeakHashMap;

@CommandPermission("command.profileselection")
@CommandAlias("profileselection|ps")
public class ProfileSelectionCommand extends BaseCommand {

  private static class Selection {
    private Location locationA;
    private Location locationB;
  }

  private final WeakHashMap<Player, Selection> selectionMap = new WeakHashMap<>();

  @Default
  public void onProfileSelectionEnter(Player player) {
    ProfileSelectionManager profileSelectionManager = CollegeCore.getProfileSelectionManager();
    String selected = profileSelectionManager.enterSelection(player);
    if (selected != null) {
      Msg.sendAdminInfo(player, "Entering profile selection: {}", selected);
    } else {
      Msg.sendError(player, "There is currently no free spot for your profile selection.");
    }
  }

  @Subcommand("playerpos")
  public void onPosOne(Player player) {
    this.selectionMap.computeIfAbsent(player, key -> new Selection()).locationA = player.getLocation();
    Msg.sendAdminInfo(player, "You have selected the position for the player.");
  }

  @Subcommand("displaypos")
  public void onPosTwo(Player player) {
    this.selectionMap.computeIfAbsent(player, key -> new Selection()).locationB = player.getLocation();
    Msg.sendAdminInfo(player, "You have selected the position for the profile display.");
  }

  @Subcommand("create")
  @Syntax("<Name>")
  public void onCreate(Player player, String name) {
    Selection selection = this.selectionMap.computeIfAbsent(player, key -> new Selection());
    if (selection.locationA == null || selection.locationB == null) {
      Msg.sendError(player, "You have to select the player and display positions first.");
      return;
    }
    ProfileSelectionLocation selectionLocation = new ProfileSelectionLocation(name, selection.locationA, selection.locationB);
    CollegeCore.getProfileSelectionManager().addSelection(selectionLocation);
    Msg.sendAdminInfo(player, "You have created the {} selection.", name);
  }

  @Subcommand("tp")
  @Syntax("<Name>")
  public void onTp(Player player, String name) {
    ProfileSelectionManager profileSelectionManager = CollegeCore.getProfileSelectionManager();
    if (!profileSelectionManager.nameExists(name)) {
      Msg.sendError(player, "There is no location with the name {}.", name);
      return;
    }
    profileSelectionManager.tpTo(player, name).thenRun(() -> {
      Msg.sendAdminInfo(player, "You have been teleported to {}.", name);
    });
  }

  @Subcommand("remove")
  @Syntax("<Name>")
  @CommandCompletion("@ProfileSelectionLocations")
  public void onRemove(Player player, String name) {
    ProfileSelectionManager profileSelectionManager = CollegeCore.getProfileSelectionManager();
    if (!profileSelectionManager.nameExists(name)) {
      Msg.sendError(player, "There is no location with the name {}.", name);
      return;
    }
    profileSelectionManager.removeLocation(name);
    Msg.sendAdminInfo(player, "You have deleted the {} location.");
  }

  @Subcommand("list")
  public void onList(Player player) {
    Msg.sendAdminInfo(player, "Profile Selection Locations:");
    CollegeCore.getProfileSelectionManager().getLocationNames().forEach(name -> player.sendMessage("§e- " + name));
  }

}
