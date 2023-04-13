package net.collegemc.mc.core.quests.rewards;

import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.core.economy.EconomyOperation;
import net.collegemc.mc.core.quests.QuestReward;
import net.collegemc.mc.libs.hooks.itemsadder.CollegemcSymbol;
import org.bukkit.entity.Player;

public class MoneyReward implements QuestReward {

  private final double amount;

  public MoneyReward(double amount) {
    this.amount = amount;
  }

  @Override
  public void accept(Player player) {
    ActiveCollegeUser.of(player).applyToEconomyAccount(EconomyOperation.ADD, amount);
  }

  @Override
  public String getDescription() {
    return "§e" + amount + "§f" + CollegemcSymbol.COIN;
  }
}
