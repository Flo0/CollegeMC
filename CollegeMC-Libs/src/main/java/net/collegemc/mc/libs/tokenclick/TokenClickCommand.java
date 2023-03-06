package net.collegemc.mc.libs.tokenclick;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Private;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@Private
@RequiredArgsConstructor
@CommandAlias("tokenclick")
public class TokenClickCommand extends BaseCommand {

  private final TokenActionManager tokenActionManager;

  @Default
  public void onCommand(final Player player, final String token) {
    this.tokenActionManager.applyAction(player, token);
  }

}