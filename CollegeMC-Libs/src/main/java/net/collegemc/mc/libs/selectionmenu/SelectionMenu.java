package net.collegemc.mc.libs.selectionmenu;

import lombok.Getter;

import java.util.concurrent.CompletableFuture;

public abstract class SelectionMenu {

  @Getter
  private final TieDown tieDown;

  protected SelectionMenu(TieDown tieDown) {
    this.tieDown = tieDown;
  }

  protected boolean isTicked() {
    return false;
  }

  protected void onTick() {

  }

  protected void swingSelect() {

  }

  public abstract CompletableFuture<Void> onStart();

  public abstract CompletableFuture<Void> onEnd(boolean now);

}
