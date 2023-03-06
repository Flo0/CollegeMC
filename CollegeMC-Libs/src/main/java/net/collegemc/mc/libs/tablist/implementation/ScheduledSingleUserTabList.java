package net.collegemc.mc.libs.tablist.implementation;

import java.util.UUID;

public abstract class ScheduledSingleUserTabList extends SingleUserTabList {

  private final int scheduledInterval;
  private long counter = 0L;

  public ScheduledSingleUserTabList(UUID userID, int scheduledInterval) {
    super(userID);
    this.scheduledInterval = scheduledInterval;
  }

  public abstract void tickAction();

  public final void onTick() {
    if (++this.counter % this.scheduledInterval == 0) {
      this.tickAction();
    }
  }

}
