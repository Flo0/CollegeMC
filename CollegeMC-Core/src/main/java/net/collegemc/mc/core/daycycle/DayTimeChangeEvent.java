package net.collegemc.mc.core.daycycle;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DayTimeChangeEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  @Getter
  private final DayCycleManager.DayTime oldTime;
  @Getter
  private final DayCycleManager.DayTime newTime;

  public DayTimeChangeEvent(DayCycleManager.DayTime oldTime, DayCycleManager.DayTime newTime) {
    this.oldTime = oldTime;
    this.newTime = newTime;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }
}
