package net.collegemc.mc.core.daycycle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.Duration;
import java.time.Instant;

public class DayCycleManager {

  private static final long dayCycleLength = 12000L;
  private static final long nightCycleLength = 12000L;
  private final Duration dayLength;
  private final Duration nightLength;
  private Instant lastDayStart;
  private long currentMinecraftDayTime;
  private DayTime lastDayTime;

  public DayCycleManager() {
    this.dayLength = Duration.ofSeconds(1200);
    this.nightLength = Duration.ofSeconds(60);
    this.lastDayStart = Instant.now();
    this.lastDayTime = DayTime.NIGHT;
    TaskManager.runTaskTimer(this::tick, 0, 1);
  }

  public void tick() {
    adjustDayTime();
    for (World world : Bukkit.getWorlds()) {
      world.setTime(currentMinecraftDayTime);
    }
  }

  private void adjustDayTime() {
    Duration timeSinceLastDayStart = Duration.between(lastDayStart, Instant.now());
    if (timeSinceLastDayStart.compareTo(dayLength) < 0) {
      currentMinecraftDayTime = (long) (timeSinceLastDayStart.toMillis() / (double) dayLength.toMillis() * dayCycleLength);
    } else if (timeSinceLastDayStart.compareTo(dayLength) >= 0 && timeSinceLastDayStart.compareTo(dayLength.plus(nightLength)) < 0) {
      currentMinecraftDayTime = (long) (dayCycleLength + (timeSinceLastDayStart.minus(dayLength).toMillis() / (double) nightLength.toMillis() * nightCycleLength));
    } else {
      currentMinecraftDayTime = 0;
      lastDayStart = Instant.now();
    }
    DayTime currentDayTime = calculateCurrentDayTime();
    if (currentDayTime != lastDayTime) {
      DayTime oldTime = lastDayTime;
      lastDayTime = currentDayTime;
      Bukkit.getPluginManager().callEvent(new DayTimeChangeEvent(oldTime, currentDayTime));
    }
  }

  public DayTime getDayTime() {
    return lastDayTime;
  }

  private DayTime calculateCurrentDayTime() {
    if (currentMinecraftDayTime >= DayTime.MORNING.getStartTickTimestamp() && currentMinecraftDayTime < DayTime.DAY.getStartTickTimestamp()) {
      return DayTime.MORNING;
    } else if (currentMinecraftDayTime >= DayTime.DAY.getStartTickTimestamp() && currentMinecraftDayTime < DayTime.EVENING.getStartTickTimestamp()) {
      return DayTime.DAY;
    } else if (currentMinecraftDayTime >= DayTime.EVENING.getStartTickTimestamp() && currentMinecraftDayTime < DayTime.NIGHT.getStartTickTimestamp()) {
      return DayTime.EVENING;
    } else {
      return DayTime.NIGHT;
    }
  }

  @Getter
  @AllArgsConstructor
  public enum DayTime {
    MORNING(0),
    DAY(4500),
    EVENING(10500),
    NIGHT(13500);

    private final long startTickTimestamp;
  }

}
