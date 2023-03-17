package net.collegemc.mc.libs.tokenclick;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class TokenActionDomain {

  private final Player player;
  private final Map<String, Consumer<Player>> actionMap = new HashMap<>();

  public void addAction(final String token, final Consumer<Player> action) {
    this.actionMap.put(token, action);
  }

  public void applyAction(final String token) {
    final Consumer<Player> action = this.actionMap.remove(token);
    if (action != null) {
      action.accept(this.player);
    }
  }

}
