package net.collegemc.mc.libs.hooks.itemsadder;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public enum CollegemcSound {

  CACHING("caching");

  private final String key;

  public void play(Player player) {
    ItemsAdderHook.playSound(player, key);
  }

  public void play(Player player, float volume, float pitch) {
    ItemsAdderHook.playSound(player, key, volume, pitch);
  }

}
