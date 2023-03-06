package net.collegemc.mc.libs.tablist;

import net.collegemc.mc.libs.tablist.implementation.ScheduledSingleUserTabList;
import net.collegemc.mc.libs.tasks.TaskManager;

import java.util.ArrayList;
import java.util.List;

public class TabListScheduler implements Runnable {

  private static final TabListScheduler INSTANCE = new TabListScheduler();

  protected static TabListScheduler getInstance() {
    return INSTANCE;
  }

  private final List<ScheduledSingleUserTabList> tabListList = new ArrayList<>();

  private TabListScheduler() {
    TaskManager.runTaskTimer(this, 1, 1);
  }

  protected void register(ScheduledSingleUserTabList tabList) {
    this.tabListList.add(tabList);
  }

  protected void unregister(ScheduledSingleUserTabList tabList) {
    this.tabListList.remove(tabList);
  }

  @Override
  public void run() {
    this.tabListList.forEach(ScheduledSingleUserTabList::onTick);
  }
}
