package net.collegemc.mc.core.gson;

import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.gson.PostDeserializationAdapterFactory;
import net.collegemc.common.gson.adapters.ClassAdapter;
import net.collegemc.common.gson.adapters.UUIDAdapter;
import net.collegemc.mc.core.gson.adapters.ItemStackAdapter;
import net.collegemc.mc.core.gson.adapters.LocationAdapter;
import net.collegemc.mc.core.gson.adapters.WorldAdapter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CollegeGsonSerializer extends GsonSerializer {

  public CollegeGsonSerializer() {
    this.registerTypeAdapterFactory(new PostDeserializationAdapterFactory());
    this.registerTypeAdapter(UUID.class, new UUIDAdapter());
    this.registerTypeAdapter(Class.class, new ClassAdapter());
    this.registerTypeAdapter(Location.class, new LocationAdapter());
    this.registerTypeAdapter(ItemStack.class, new ItemStackAdapter());
    this.registerTypeHierarchyAdapter(World.class, new WorldAdapter());
  }

}
