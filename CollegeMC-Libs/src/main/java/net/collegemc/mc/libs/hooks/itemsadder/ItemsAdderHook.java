package net.collegemc.mc.libs.hooks.itemsadder;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ItemsAdderHook {

  @Getter
  protected static boolean loaded = false;

  public static void blackFade(Player player, int fadeIn, int stay, int fadeOut) {
    String command = "screeneffect fullscreen BLACK %d %d %d nofreeze %s".formatted(fadeIn, stay, fadeOut, player.getName());
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
  }

  public static void playSound(Player player, String soundName) {
    playSound(player, soundName, 1, 1);
  }

  public static void playSound(Player player, String soundName, float volume, float pitch) {
    playSound(player, soundName, Sound.Source.MASTER, volume, pitch);
  }

  public static void playSound(Player player, String soundName, Sound.Source source, float volume, float pitch) {
    Key key = Key.key("collegemc." + soundName);
    Sound sound = Sound.sound(key, source, volume, pitch);
    player.playSound(sound);
  }

  public static String getFontImage(String name) {
    return new FontImageWrapper("collegemc:" + name).getString();
  }

}
