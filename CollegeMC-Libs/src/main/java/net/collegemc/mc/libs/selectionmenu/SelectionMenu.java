package net.collegemc.mc.libs.selectionmenu;

import lombok.Getter;
import org.bukkit.Location;

import java.util.concurrent.CompletableFuture;

public abstract class SelectionMenu {

  @Getter
  private final Location selectionLocation;
  @Getter
  private final TieDown tieDown;
  @Getter
  protected Location returnLocation;
  @Getter
  private boolean teleportOnEnd;

  protected SelectionMenu(Location returnLocation, Location selectionLocation, TieDown tieDown, boolean teleportOnEnd) {
    this.returnLocation = returnLocation;
    this.selectionLocation = selectionLocation;
    this.tieDown = tieDown;
  }

  protected SelectionMenu(Location returnLocation, Location selectionLocation, TieDown tieDown) {
    this(returnLocation, selectionLocation, tieDown, true);
  }

  protected boolean isTicked() {
    return false;
  }

  protected void onTick() {

  }

  protected void swingSelect() {
    
  }

  public abstract CompletableFuture<Void> preStart();

  public abstract CompletableFuture<Void> preEnd();

}
