package net.collegemc.mc.libs.hooks.itemsadder;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ItemsAdderHook {

  @Getter
  protected static boolean loaded = false;

  public static void blackFade(Player player, int fadeIn, int stay, int fadeOut) {
    String command = "screeneffect fullscreen BLACK %d %d %d nofreeze %s".formatted(fadeIn, stay, fadeOut, player.getName());
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
  }

}
