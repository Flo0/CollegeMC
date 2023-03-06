package net.collegemc.mc.libs.holograms.abstraction;

import com.google.common.base.Preconditions;
import net.collegemc.mc.libs.spigot.UtilChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHologram implements Hologram {

  private static final double LINE_SPACE = 0.275;

  private final List<HologramLine> hologramLines = new ArrayList<>();
  private final String name;
  private Location location;

  public AbstractHologram(Location location, String name) {
    this.name = name;
    this.location = location;
  }

  private Location getRelativeLocationForIndex(int index) {
    return this.location.clone().add(0, -LINE_SPACE * index, 0);
  }

  @Override
  public String getId() {
    return this.name;
  }

  @Override
  public int size() {
    return this.hologramLines.size();
  }

  @Override
  public void addLine(String line) {
    int nextIndex = this.size();
    Location lineLocation = this.getRelativeLocationForIndex(nextIndex);
    HologramLine hologramLine = this.createLine(lineLocation);
    hologramLine.setText(line);
    this.hologramLines.add(hologramLine);

    Chunk chunk = this.location.getChunk();
    Bukkit.getOnlinePlayers().forEach(player -> {
      if (UtilChunk.isChunkInView(player, chunk)) {
        hologramLine.showTo(player);
      }
    });
  }

  @Override
  public void setLine(int index, String line) {
    Preconditions.checkArgument(index < this.size());
    HologramLine hologramLine = this.hologramLines.get(index);
    hologramLine.setText(line);

    Chunk chunk = this.location.getChunk();
    Bukkit.getOnlinePlayers().forEach(player -> {
      if (UtilChunk.isChunkInView(player, chunk)) {
        hologramLine.updateTextFor(player);
      }
    });
  }

  @Override
  public String getLine(int index) {
    Preconditions.checkArgument(index < this.size());
    return this.hologramLines.get(index).getText();
  }

  @Override
  public Location getLocation() {
    return this.location;
  }

  @Override
  public void teleport(Location target) {
    Chunk fromChunk = this.location.getChunk();
    Chunk toChunk = target.getChunk();
    this.location = target;
    for (int index = 0; index < this.size(); index++) {
      Location lineLoc = this.getRelativeLocationForIndex(index);
      HologramLine hologramLine = this.hologramLines.get(index);
      hologramLine.teleport(lineLoc);
      Bukkit.getOnlinePlayers().forEach(player -> {
        if (UtilChunk.isChunkInView(player, fromChunk) || UtilChunk.isChunkInView(player, toChunk)) {
          hologramLine.updateLocationFor(player);
        }
      });
    }
  }

  @Override
  public void showTo(Player player) {
    this.hologramLines.forEach(line -> line.showTo(player));
  }

  @Override
  public void hideFrom(Player player) {
    this.hologramLines.forEach(line -> line.hideFrom(player));
  }

  @Override
  public void removeLine(int index) {
    Preconditions.checkArgument(index < this.size());
    HologramLine line = this.hologramLines.remove(index);
    Chunk chunk = this.location.getChunk();

    Bukkit.getOnlinePlayers().forEach(player -> {
      if (UtilChunk.isChunkInView(player, chunk)) {
        line.hideFrom(player);
      }
    });

    this.teleport(this.location);
  }

  protected abstract HologramLine createLine(Location location);
}
