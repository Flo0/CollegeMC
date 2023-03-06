package net.collegemc.mc.libs.tokenclick;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class TokenActionDomain {

  private final Player player;
  private final Cache<String, Consumer<Player>> actionMap = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(15)).build();

  public void addAction(final String token, final Consumer<Player> action) {
    this.actionMap.put(token, action);
  }

  public void applyAction(final String token) {
    final Consumer<Player> action = this.actionMap.getIfPresent(token);
    if (action != null) {
      action.accept(this.player);
    }
  }

}
