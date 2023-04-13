package net.collegemc.mc.core.economy;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Values;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.libs.hooks.itemsadder.CollegemcSymbol;
import net.collegemc.mc.libs.messaging.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

@CommandAlias("economy|eco")
public class EconomyCommand extends BaseCommand {

  @Default
  public void onCommand(Player player) {
    EconomyAccount account = ActiveCollegeUser.of(player).getEconomyAccountSnapshot();
    if (account == null) {
      Msg.sendAdminInfo(player, "You do not have an economy account!");
    } else {
      TextComponent componentMessage = Component.text("Your balance: ");
      componentMessage = componentMessage.color(NamedTextColor.WHITE);
      TextComponent amountComponent = Component.text(account.getBalance());
      amountComponent = amountComponent.color(NamedTextColor.YELLOW);
      TextComponent coinComponent = Component.text(CollegemcSymbol.COIN.get());
      coinComponent = coinComponent.color(TextColor.color(255, 255, 253));

      Msg.sendInfo(player, componentMessage.append(amountComponent).append(coinComponent));
    }
  }

  @Subcommand("add")
  @CommandCompletion("@ActiveCollegeUser @nothing")
  public void onAdd(Player player, @Values("@ActiveCollegeUser") ActiveCollegeUser target, double amount) {
    Msg.sendAdminInfo(player, "Waiting for transaction result...");
    target.applyToEconomyAccount(EconomyOperation.ADD, amount).thenAccept(result -> {
      if (result.isSuccess()) {
        Msg.sendAdminInfo(player, "Successfully added {} %s to {}".formatted(CollegemcSymbol.COIN), amount, target.resolveName());
      } else {
        Msg.sendAdminInfo(player, "Result: {}", result);
      }
    });
  }

  @Subcommand("remove")
  @CommandCompletion("@ActiveCollegeUser @nothing")
  public void onRemove(Player player, @Values("@ActiveCollegeUser") ActiveCollegeUser target, double amount) {
    Msg.sendAdminInfo(player, "Waiting for transaction result...");
    target.applyToEconomyAccount(EconomyOperation.SUBTRACT, amount).thenAccept(result -> {
      if (result.isSuccess()) {
        Msg.sendAdminInfo(player, "Successfully removed {} %s from {}".formatted(CollegemcSymbol.COIN), amount, target.resolveName());
      } else {
        Msg.sendAdminInfo(player, "Result: {}", result);
      }
    });
  }

}
