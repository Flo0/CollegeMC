package net.collegemc.mc.core.gson;

import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.gson.PostDeserializationAdapterFactory;
import net.collegemc.mc.core.gson.adapters.ItemStackAdapter;
import net.collegemc.mc.core.gson.adapters.LocationAdapter;
import net.collegemc.mc.core.gson.adapters.WorldAdapter;
import net.collegemc.mc.core.quests.QuestReward;
import net.collegemc.mc.core.quests.QuestTarget;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

public class CollegeGsonSerializer extends GsonSerializer {

  public CollegeGsonSerializer() {
    this.registerTypeAdapterFactory(new PostDeserializationAdapterFactory());
    this.registerTypeAdapter(Location.class, new LocationAdapter());
    this.registerTypeAdapter(ItemStack.class, new ItemStackAdapter());
    this.registerTypeHierarchyAdapter(World.class, new WorldAdapter());
    this.registerAbstractTypeHierarchyAdapter(QuestReward.class);
    this.registerAbstractTypeHierarchyAdapter(QuestTarget.class);
  }

}
