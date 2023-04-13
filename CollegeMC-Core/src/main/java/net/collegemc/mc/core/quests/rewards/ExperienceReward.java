package net.collegemc.mc.core.quests.rewards;

import net.collegemc.mc.core.quests.QuestReward;
import org.bukkit.entity.Player;

public class ExperienceReward implements QuestReward {

  private final int amount;

  public ExperienceReward(int amount) {
    this.amount = amount;
  }

  @Override
  public String getDescription() {
    return "§e" + amount + " experience.";
  }

  @Override
  public void accept(Player player) {
    player.setTotalExperience(player.getTotalExperience() + amount);
  }
}
