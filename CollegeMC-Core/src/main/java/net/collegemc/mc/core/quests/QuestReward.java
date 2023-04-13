package net.collegemc.mc.core.quests;

import org.bukkit.entity.Player;

import java.util.function.Consumer;

public interface QuestReward extends Consumer<Player> {

  String getDescription();

}
