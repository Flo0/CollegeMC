package net.collegemc.mc.core.quests;

import lombok.Getter;

import java.util.function.Function;

public abstract class QuestTarget<T> implements Function<T, Integer> {

  @Getter
  private final Class<T> targetType;
  @Getter
  private final int targetProgress;
  @Getter
  private int currentProgress;

  protected QuestTarget(Class<T> targetType, int targetProgress) {
    this.targetType = targetType;
    this.targetProgress = targetProgress;
  }

  public boolean reactOn(T target) {
    Integer progress = this.apply(target);
    if (progress != null) {
      this.currentProgress += progress;
      this.currentProgress = Math.min(this.currentProgress, this.targetProgress);
    }
    return progress != null;
  }

  public boolean isComplete() {
    return this.currentProgress >= this.targetProgress;
  }

  public double getProgressPercent() {
    return 1.0 / this.targetProgress * this.currentProgress;
  }

  public abstract String getShortDescription();
}
