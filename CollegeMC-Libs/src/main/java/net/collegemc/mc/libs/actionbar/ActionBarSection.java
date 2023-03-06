package net.collegemc.mc.libs.actionbar;

import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;


public class ActionBarSection {

  protected ActionBarSection() {
    this.actionLineSet = new TreeSet<>();
    this.tempTasks = new HashMap<>();
    this.addLayer(ActionLine.defaultLine());
  }

  private final SortedSet<ActionLine> actionLineSet;
  private final Map<ActionLine, BukkitTask> tempTasks;

  public void addLayer(String layerId, int priority, Supplier<String> lineSupplier) {
    this.addLayer(new ActionLine(layerId, priority, lineSupplier));
  }

  public void addLayer(ActionLine line) {
    this.actionLineSet.add(line);
  }

  public void removeLayer(ActionLine line) {
    this.actionLineSet.remove(line);
    this.tempTasks.remove(line);
  }

  public void remove(String key) {
    this.removeLayer(new ActionLine(key, 0, (Supplier<String>) null));
  }

  public void addTempLayer(long lifeTicks, ActionLine line) {
    this.addLayer(line);
    Optional.ofNullable(this.tempTasks.remove(line)).ifPresent(BukkitTask::cancel);
    this.tempTasks.put(line, TaskManager.runTaskLater(() -> this.removeLayer(line), lifeTicks));
  }

  public ActionLine getMostSignificantLayer() {
    return this.actionLineSet.first();
  }

}
