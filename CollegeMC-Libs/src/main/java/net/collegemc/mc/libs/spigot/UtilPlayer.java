package net.collegemc.mc.libs.spigot;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

@UtilityClass
public class UtilPlayer {

  public static void playUIClick(Player player) {
    Sound sound = Sound.sound()
            .pitch(0.75F)
            .volume(0.75F)
            .type(org.bukkit.Sound.UI_BUTTON_CLICK.key())
            .build();
    player.playSound(sound, Sound.Emitter.self());
  }

  public static void playSuccessPling(Player player) {
    Sound sound = Sound.sound()
            .pitch(0.75F)
            .volume(1.25F)
            .type(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING.key())
            .build();
    player.playSound(sound, Sound.Emitter.self());
  }

  public static void playFailurePling(Player player) {
    Sound sound = Sound.sound()
            .pitch(0.75F)
            .volume(0.66F)
            .type(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING.key())
            .build();
    player.playSound(sound, Sound.Emitter.self());
  }

}
