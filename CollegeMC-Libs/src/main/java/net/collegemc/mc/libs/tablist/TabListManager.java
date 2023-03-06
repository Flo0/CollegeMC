package net.collegemc.mc.libs.tablist;


import com.google.common.collect.Maps;
import lombok.Setter;
import net.collegemc.mc.libs.tablist.abstraction.TabView;
import net.collegemc.mc.libs.tablist.implementation.AbstractTabList;
import net.collegemc.mc.libs.tablist.implementation.ScheduledSingleUserTabList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.function.Function;

public class TabListManager {

  public TabListManager(final JavaPlugin plugin, final Function<Player, AbstractTabList> defaultTabListProvider) {
    this.tabViewMap = Maps.newHashMap();
    Bukkit.getPluginManager().registerEvents(new TabListListener(this), plugin);
    this.defaultTabListProvider = defaultTabListProvider;
  }

  private final Map<Player, TabView> tabViewMap;
  @Setter
  private Function<Player, AbstractTabList> defaultTabListProvider;

  public TabView getView(final Player player) {
    return this.tabViewMap.get(player);
  }

  protected void addPlayer(final Player player) {
    this.tabViewMap.put(player, new TabView(player));
    final AbstractTabList tabList = this.defaultTabListProvider.apply(player);
    if (tabList == null) {
      return;
    }
    this.tabViewMap.get(player).setAndUpdate(tabList);
    if (tabList instanceof ScheduledSingleUserTabList scheduled) {
      TabListScheduler.getInstance().register(scheduled);
      scheduled.onPlayerAdd();
    }
  }

  protected void removePlayer(final Player player) {
    TabView view = this.tabViewMap.remove(player);
    if (view != null && view.getTablist() instanceof ScheduledSingleUserTabList scheduled) {
      TabListScheduler.getInstance().unregister(scheduled);
      scheduled.onPlayerRemove();
    }
  }

}