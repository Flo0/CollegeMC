package net.collegemc.mc.core.widgetdisplays.craftle;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import net.collegemc.mc.core.CollegeCore;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandAlias("craftle")
public class CraftleDebugCommand extends BaseCommand {

  @Default
  public void onDefault(Player sender) {
    CollegeCore.getCraftleManager().startCraftle(new Location(sender.getWorld(), -7, 101, 26), sender.getPlayer());
  }

}
