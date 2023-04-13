package net.collegemc.mc.libs.spigot.events.kill;

import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerKillEntityEvent extends PlayerEvent implements Cancellable {

  private static final HandlerList HANDLERS = new HandlerList();

  @Getter
  private final Entity entity;
  private boolean cancelled;

  public PlayerKillEntityEvent(@NotNull Player who, Entity entity) {
    super(who);
    this.entity = entity;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLERS;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean value) {
    this.cancelled = value;
  }
}
