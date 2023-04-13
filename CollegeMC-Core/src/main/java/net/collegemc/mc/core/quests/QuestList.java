package net.collegemc.mc.core.quests;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.network.data.college.CollegeProfile;
import net.collegemc.common.network.data.college.ProfileId;
import net.collegemc.mc.libs.advancements.AdvancementManager;
import net.collegemc.mc.libs.tasks.TaskManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestList {

  @Getter
  private final ProfileId profileId;
  private final Map<String, Quest> questMap;
  private final transient BossBar progressBar;
  private transient BukkitTask hideTask;

  public QuestList(ProfileId profileId) {
    this.profileId = profileId;
    this.questMap = new HashMap<>();
    this.progressBar = BossBar.bossBar(Component.text("_ERROR_"), 0.0F, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
  }

  public QuestList() {
    this(null);
  }

  public boolean hasQuest(String id) {
    return this.questMap.containsKey(id);
  }

  public void addQuest(Quest quest) {
    Player player = resolveBukkitPlayer();
    Preconditions.checkArgument(player != null, "Player is null");
    AdvancementManager.sendAdvancement(player, quest.getIcon(), quest.getId(), AdvancementManager.FrameType.TASK);
    this.questMap.put(quest.getId(), quest);
  }

  public void removeQuest(String id) {
    this.questMap.remove(id);
  }

  private void showProgress(Quest quest) {
    Player player = resolveBukkitPlayer();
    double progress = quest.getProgress();
    progressBar.name(Component.text("§f" + quest.getId() + " §e" + (int) progress + "%"));
    progressBar.progress((float) progress);
    if (hideTask == null) {
      player.showBossBar(progressBar);
    } else {
      hideTask.cancel();
    }
    hideTask = TaskManager.runTaskLater(() -> {
      hideTask = null;
      player.hideBossBar(progressBar);
    }, 50);
  }

  public <T> void propagateEvent(T event) {
    getQuests().forEach(quest -> {
      if (quest.reactOn(event)) {
        showProgress(quest);
      } else {
        return;
      }
      if (quest.isDone()) {
        completeQuest(quest);
      }
    });
  }

  private void completeQuest(Quest quest) {
    Player player = resolveBukkitPlayer();
    removeQuest(quest.getId());
    quest.releaseRewards(player);
    Key key = org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE.key();
    Sound sound = Sound.sound()
            .type(key)
            .volume(1.0F)
            .pitch(1.0F)
            .source(Sound.Source.MASTER)
            .build();
    player.playSound(sound, Sound.Emitter.self());
    Component major = Component.text("Task completed");
    Component minor = Component.text(quest.getId());
    Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(2000), Duration.ofMillis(500));
    Title title = Title.title(major, minor, times);
    player.showTitle(title);
  }

  private Player resolveBukkitPlayer() {
    CollegeProfile profile = GlobalGateway.getCollegeProfileManager().getLoaded(profileId);
    Preconditions.checkArgument(profile != null, "Profile is not loaded");
    Player player = Bukkit.getPlayer(profile.getMinecraftUserId());
    Preconditions.checkArgument(player != null, "Player is not online");
    return player;
  }

  public List<Quest> getQuests() {
    return List.copyOf(this.questMap.values());
  }
}
