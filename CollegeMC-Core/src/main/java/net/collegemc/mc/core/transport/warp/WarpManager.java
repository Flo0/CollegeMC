package net.collegemc.mc.core.transport.warp;

import net.collegemc.mc.libs.tasks.MongoBackedMap;
import org.jetbrains.annotations.NotNull;

import java.io.Flushable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class WarpManager implements Iterable<Warp>, Flushable {

  public static final String NAMESPACE = "Warps";

  private final MongoBackedMap<String, Warp> warpMap;

  public WarpManager() {
    this.warpMap = new MongoBackedMap<>(new HashMap<>(), NAMESPACE, String.class, Warp.class);
    this.warpMap.loadDataFromRemote();
  }

  public void addWarp(Warp warp) {
    this.warpMap.put(warp.getName(), warp);
  }

  public void removeWarp(String warpName) {
    this.warpMap.remove(warpName);
  }

  public Warp getWarp(String warpName) {
    return this.warpMap.get(warpName);
  }

  public List<String> getWarpNames() {
    return List.copyOf(this.warpMap.keySet());
  }

  @NotNull
  @Override
  public Iterator<Warp> iterator() {
    return List.copyOf(this.warpMap.values()).iterator();
  }

  @Override
  public void flush() {
    this.warpMap.saveDataToRemote();
  }
}
