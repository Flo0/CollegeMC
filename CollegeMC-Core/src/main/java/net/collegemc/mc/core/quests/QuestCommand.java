package net.collegemc.mc.core.quests;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.google.common.base.Preconditions;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.core.quests.rewards.ExperienceReward;
import net.collegemc.mc.core.quests.rewards.MoneyReward;
import net.collegemc.mc.core.quests.targets.SimpleBlockBreakTarget;
import net.collegemc.mc.core.quests.targets.SimpleKillEntityQuest;
import net.collegemc.mc.core.quests.ui.QuestMainGUI;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@CommandAlias("tasks|quest|quests")
public class QuestCommand extends BaseCommand {

  @Default
  public void onDefault(Player player) {
    new QuestMainGUI(player).openFor(player);
  }

  @Subcommand("debug")
  public void onDebug(Player player) {
    ItemStack icon = new ItemStack(Material.STONE);
    Quest quest = new Quest("Sticks and Stones", icon);
    quest.addQuestTarget(new SimpleBlockBreakTarget(Material.STONE, 32));
    quest.addQuestTarget(new SimpleBlockBreakTarget(Material.OAK_LOG, 16));
    quest.addQuestTarget(new SimpleKillEntityQuest(EntityType.SKELETON, 8));
    quest.addReward(new MoneyReward(2400));
    quest.addReward(new ExperienceReward(125));

    quest.setDescriptionRaw(List.of(
            "§fYou have been tasked with gathering some materials",
            "§ffor a new project.",
            "§fEmanuel has asked you to gather §e32 stone§f,",
            "§e16 oak logs§f, and kill §e8 skeletons§f for their §ebones§f.",
            "§fYou will be rewarded after delivering the resources.",
            "§eSkeletons §fcan be found near the §6old mine§f."
    ));

    QuestList questList = ActiveCollegeUser.of(player).getQuestList();
    Preconditions.checkArgument(questList != null, "QuestList is null");
    questList.addQuest(quest);
  }

}
