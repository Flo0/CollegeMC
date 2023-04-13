package net.collegemc.mc.core.quests;

import lombok.Getter;
import net.collegemc.common.utils.StringUtils;
import net.collegemc.mc.libs.spigot.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Quest {

  @Getter
  private final String id;
  private final ItemStack icon;
  private final Map<Class<?>, List<QuestTarget<?>>> questTargetMap;
  private final List<QuestReward> rewards = new ArrayList<>();
  private final List<TextComponent> description = new ArrayList<>();
  @Getter
  private boolean done;

  public Quest(String id, ItemStack icon) {
    this.id = id;
    this.icon = icon;
    this.questTargetMap = new HashMap<>();
  }

  public Quest() {
    this("_NO_ID_", null);
  }

  public void setDescriptionRaw(List<String> description) {
    setDescription(description.stream().map(Component::text).toList());
  }

  public void setDescription(List<TextComponent> description) {
    this.description.clear();
    this.description.addAll(description);
  }

  public void addQuestTarget(QuestTarget<?> questTarget) {
    questTargetMap.computeIfAbsent(questTarget.getClass(), k -> new ArrayList<>()).add(questTarget);
  }

  public void addReward(QuestReward reward) {
    this.rewards.add(reward);
  }

  public ItemStack getDetailedIcon() {
    ItemBuilder builder = ItemBuilder.of(getIcon());

    builder.lore("");
    if (!this.description.isEmpty()) {
      builder.lore(this.description);
      builder.lore("");
    }

    return builder.build();
  }

  public ItemStack getIcon() {
    ItemBuilder builder = ItemBuilder.of(this.icon);

    builder.name("§6" + this.id);
    builder.lore("");

    builder.lore("§fTasks:");
    for (List<QuestTarget<?>> targets : this.questTargetMap.values()) {
      for (QuestTarget<?> target : targets) {
        builder.lore("§7- §f" + target.getShortDescription() + " §f[§7" + target.getCurrentProgress() + "§f/§7" + target.getTargetProgress() + "§f]");
        String progressBar = "§f[" + StringUtils.progressBar("§a", "§c", 50, target.getProgressPercent()) + "§f]";
        builder.lore(progressBar);
        builder.lore("");
      }
    }

    builder.lore("§fYou will be rewarded with:");
    for (QuestReward reward : this.rewards) {
      builder.lore("§7- " + reward.getDescription());
    }

    return builder.build();
  }

  @SuppressWarnings("unchecked")
  public <T> boolean reactOn(T event) {
    boolean progressChanged = false;

    if (done) {
      return progressChanged;
    }

    List<QuestTarget<?>> questTargets = this.questTargetMap.get(event.getClass());
    if (questTargets == null) {
      return progressChanged;
    }

    boolean completionStateChanged = false;
    for (QuestTarget<?> questTarget : questTargets) {
      if (((QuestTarget<T>) questTarget).reactOn(event)) {
        progressChanged = true;
      }
      if (questTarget.isComplete()) {
        completionStateChanged = true;
      }
    }

    if (!completionStateChanged) {
      return progressChanged;
    }

    for (List<QuestTarget<?>> targets : this.questTargetMap.values()) {
      for (QuestTarget<?> target : targets) {
        if (!target.isComplete()) {
          return progressChanged;
        }
      }
    }

    this.done = true;
    return progressChanged;
  }

  public void releaseRewards(@NotNull Player player) {
    this.rewards.forEach(reward -> reward.accept(player));
  }

  public double getProgress() {
    double progress = 0;
    for (List<QuestTarget<?>> targets : this.questTargetMap.values()) {
      for (QuestTarget<?> target : targets) {
        progress += target.getProgressPercent();
      }
    }
    return progress / this.questTargetMap.size();
  }
}
