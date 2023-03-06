package net.collegemc.mc.libs.resourcepack.assembly;

import org.bukkit.Material;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public record BlockModel(String modelName, Material baseMaterial, String blockStateApplicant, File originalFile) {

  private static final Map<String, BlockModel> namedModelMap = new HashMap<>();

  public static Collection<BlockModel> values() {
    return namedModelMap.values();
  }

  public static BlockModel valueOf(String name) {
    return namedModelMap.get(name);
  }

  public static void register(BlockModel model) {
    if (namedModelMap.containsKey(model.modelName)) {
      throw new IllegalArgumentException("Cant register model name twice: " + model.modelName);
    }
    namedModelMap.put(model.modelName, model);
  }
//  COPPER_ORE_BLOCK(Material.ORANGE_GLAZED_TERRACOTTA,"facing=north"),
//  TIN_ORE_BLOCK(Material.LIGHT_GRAY_GLAZED_TERRACOTTA,"facing=north"),
//  IRON_ORE_BLOCK(Material.RED_GLAZED_TERRACOTTA,"facing=north"),
//  TITAN_ORE_BLOCK(Material.GRAY_GLAZED_TERRACOTTA,"facing=north"),
//  SILVER_ORE_BLOCK(Material.WHITE_GLAZED_TERRACOTTA,"facing=north"),
//  STILLIT_ORE_BLOCK(Material.YELLOW_GLAZED_TERRACOTTA,"facing=north");

}