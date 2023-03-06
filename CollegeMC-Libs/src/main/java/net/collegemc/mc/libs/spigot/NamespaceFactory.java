package net.collegemc.mc.libs.spigot;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.NamespacedKey;

import java.util.Map;

public class NamespaceFactory {

  private static final Map<String, NamespacedKey> cachedKeys = new Object2ObjectOpenHashMap<>();

  public static NamespacedKey provide(final String key) {
    NamespacedKey nsk = cachedKeys.get(key);
    if (nsk == null) {
      nsk = new NamespacedKey(CollegeLibrary.getInstance(), key);
      cachedKeys.put(key, nsk);
    }
    return nsk;
  }

}
