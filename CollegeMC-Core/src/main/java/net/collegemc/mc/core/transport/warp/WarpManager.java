package net.collegemc.mc.core.transport.warp;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.collegemc.common.mongodb.MongoMap;
import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.tasks.TaskManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WarpManager {

  public static final String NAMESPACE = "Warps";

  private final Map<String, Warp> warpMap;
  private final MongoMap<String, Warp> warpMongoMap;

  public WarpManager() {
    MongoDatabase database = CollegeLibrary.getServerDatabase();
    MongoCollection<Warp> collection = database.getCollection(NAMESPACE, Warp.class);

    this.warpMap = new ConcurrentHashMap<>();
    this.warpMongoMap = new MongoMap<>(collection, CollegeCore.getGsonSerializer(), String.class);

    this.loadWarps();
  }

  public void loadWarps() {
    this.warpMap.putAll(this.warpMongoMap);
  }

  public void addWarp(Warp warp) {
    this.warpMap.put(warp.getName(), warp);
    TaskManager.runOnIOPool(() -> this.warpMongoMap.put(warp.getName(), warp));
  }

  public void removeWarp(String warpName) {
    this.warpMap.remove(warpName);
    TaskManager.runOnIOPool(() -> this.warpMongoMap.remove(warpName));
  }

  public Warp getWarp(String warpName) {
    return this.warpMap.get(warpName);
  }

  public List<String> getWarpNames() {
    return List.copyOf(this.warpMap.keySet());
  }

}
