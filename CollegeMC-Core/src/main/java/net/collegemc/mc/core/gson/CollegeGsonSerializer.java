package net.collegemc.mc.core.gson;

import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.gson.adapters.ClassAdapter;
import net.collegemc.common.gson.adapters.UUIDAdapter;
import net.collegemc.mc.core.gson.adapters.LocationAdapter;
import org.bukkit.Location;

import java.util.UUID;

public class CollegeGsonSerializer extends GsonSerializer {

  public CollegeGsonSerializer() {
    this.registerTypeAdapter(UUID.class, new UUIDAdapter());
    this.registerTypeAdapter(Class.class, new ClassAdapter());
    this.registerTypeAdapter(Location.class, new LocationAdapter());
  }

}
