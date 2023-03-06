package net.collegemc.mc.libs.tablist.implementation;

import lombok.Getter;

import java.util.UUID;

public abstract class SingleUserTabList extends AbstractTabList {

  @Getter
  protected final UUID userID;

  public SingleUserTabList(UUID userID) {
    this.userID = userID;
  }

  public abstract void onPlayerAdd();

  public abstract void onPlayerRemove();

}
