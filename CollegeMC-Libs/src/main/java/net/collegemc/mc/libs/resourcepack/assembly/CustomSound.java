package net.collegemc.mc.libs.resourcepack.assembly;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class CustomSound {

  private static final Map<String, CustomSound> namedSoundMap = new HashMap<>();

  public static Collection<CustomSound> values() {
    return namedSoundMap.values();
  }

  public static CustomSound valueOf(String name) {
    return namedSoundMap.get(name);
  }

  public static void register(CustomSound sound) {
    if (namedSoundMap.containsKey(sound.soundName)) {
      throw new IllegalArgumentException("Cant register sound name twice: " + sound.soundName);
    }
    namedSoundMap.put(sound.soundName, sound);
  }

  /*
  COINS_SOUND(),
  PEACE_SOUND(),
  TRUMPET(),
  DARK_WAVE(),
  DARK_WAVE_HIT(),
  ORC_HORN(),
  ORC_CRY(),
  HEART_BEAT_10S(),
  UNDEAD_THEME(),
  ORC_THEME(),
  ELF_THEME(),
  HUMAN_THEME(),
  DWARF_THEME(),
  CHOIRS_OF_WAR(),
  APOLLO(),
  MERAKI(),
  STORM(),
  WAR_HORN(),
  STICK_HIT_ONE(),
  STICK_HIT_TWO(),
  STICK_HIT_THREE();
   */

  @Getter
  private final File soundFile;
  @Getter
  private final String soundName;
  @Getter
  private ResourceLocation key = null;

  public void playHeadArtificialDist(final Location location, final double radius, final float pitch,
                                     final SoundSource soundCategory) {
    final double maxSquared = -(radius * radius);
    location.getNearbyPlayers(radius).forEach(
            player -> this.play(player, location, soundCategory, this.calcVolumeSquared(location, player.getLocation(), maxSquared), pitch));
  }

  private float calcVolumeSquared(final Location soundPoint, final Location distPoint, final double maxSq) {
    final double distSq = soundPoint.distanceSquared(distPoint);
    return (float) (1.0 - (distSq / maxSq));
  }

  public void stopFor(Player player) {
    this.stopFor(player, SoundSource.AMBIENT);
  }

  public void stopFor(Player player, SoundSource category) {
    ClientboundStopSoundPacket stopPacket = new ClientboundStopSoundPacket(this.key, category);
    ((CraftPlayer) player).getHandle().connection.send(stopPacket);
  }

  public void play(final Player player) {
    this.play(player, player.getEyeLocation(), SoundSource.AMBIENT, 1F, 1F);
  }

  public void play(final Player player, final Location location) {
    this.play(player, location, SoundSource.AMBIENT, 1F, 1F);
  }

  public void play(final Player player, final Location location, final float volume, final float pitch) {
    this.play(player, location, SoundSource.AMBIENT, volume, pitch);
  }

  public void play(final Player player, final Location location, final SoundSource soundCategory) {
    this.play(player, location, soundCategory, 1F, 1F);
  }

  public void play(final Player player, final SoundSource soundCategory, final float volume, final float pitch) {
    this.play(player, player.getEyeLocation(), soundCategory, volume, pitch);
  }

  public void play(final Player player, final float volume, final float pitch) {
    this.play(player, player.getEyeLocation(), SoundSource.AMBIENT, volume, pitch);
  }

  public void playAt(final Location location, final SoundCategory soundCategory, final float volume, final float pitch) {
    if (this.key == null) {
      this.key = new ResourceLocation("custom." + this.soundName.toLowerCase());
    }
    location.getWorld().playSound(location, this.key.getPath(), soundCategory, volume, pitch);
  }

  public void play(final Player player, final Location location, final SoundSource soundCategory, final float volume, final float pitch) {
    if (this.key == null) {
      this.key = new ResourceLocation("custom." + this.soundName.toLowerCase());
    }
    final Vec3 vec = new Vec3(location.getX(), location.getY(), location.getZ());
    final long seed = ThreadLocalRandom.current().nextInt();
    Holder<SoundEvent> event = Holder.direct(SoundEvent.createVariableRangeEvent(this.key));
    final ClientboundSoundPacket packet = new ClientboundSoundPacket(event, soundCategory, vec.x, vec.y, vec.z, volume, pitch, seed);
    ((CraftPlayer) player).getHandle().connection.send(packet);
  }

}
