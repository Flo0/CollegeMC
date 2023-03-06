package net.collegemc.mc.libs.resourcepack.assembly;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.Setter;
import net.collegemc.mc.libs.spigot.ItemBuilder;
import net.collegemc.mc.libs.spigot.NamespaceFactory;
import net.collegemc.mc.libs.spigot.UtilItem;
import net.collegemc.mc.libs.tablist.abstraction.TabLine;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.mineskin.data.Skin;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TextureModel {

  private static final Map<String, TextureModel> namedModelMap = new HashMap<>();

  public static Collection<TextureModel> values() {
    return namedModelMap.values();
  }

  public static TextureModel valueOf(String name) {
    return namedModelMap.get(name);
  }

  public static void register(TextureModel model) {
    if (namedModelMap.containsKey(model.modelName)) {
      throw new IllegalArgumentException("Cant register model name twice: " + model.modelName);
    }
    namedModelMap.put(model.modelName, model);
  }

  TextureModel(String modelName,
               File textureFile,
               Material baseMaterial,
               int modelID,
               boolean headEnabled,
               boolean customModelDataEnabled,
               boolean playerSkinModel
  ) {
    this.textureFile = textureFile;
    this.modelName = modelName;
    this.baseMaterial = baseMaterial;
    this.modelID = modelID;
    this.modelData = ModelData.defaultGenerated();
    this.fontMeta = FontMeta.common();
    this.boxedFontChar = new BoxedFontChar();
    this.skinEnabled = playerSkinModel || headEnabled;
    this.customModelDataEnabled = customModelDataEnabled;
    this.playerSkinModel = playerSkinModel;
  }

  TextureModel(String modelName,
               File textureFile,
               Material baseMaterial,
               int modelID,
               boolean headEnabled,
               boolean customModelDataEnabled,
               boolean playerSkinModel,
               FontMeta fontMeta
  ) {
    this.textureFile = textureFile;
    this.modelName = modelName;
    this.baseMaterial = baseMaterial;
    this.modelID = modelID;
    this.modelData = ModelData.defaultGenerated();
    this.fontMeta = fontMeta;
    this.boxedFontChar = new BoxedFontChar();
    this.skinEnabled = playerSkinModel || headEnabled;
    this.customModelDataEnabled = customModelDataEnabled;
    this.playerSkinModel = playerSkinModel;
  }

  TextureModel(String modelName,
               File textureFile,
               Material baseMaterial,
               int modelID,
               ModelData modelData,
               FontMeta fontMeta,
               boolean headEnabled,
               boolean customModelDataEnabled,
               boolean playerSkinModel
  ) {
    this.textureFile = textureFile;
    this.modelName = modelName;
    Bukkit.getServer().getName();
    this.baseMaterial = baseMaterial;
    this.modelID = modelID;
    this.modelData = modelData;
    this.fontMeta = fontMeta;
    this.boxedFontChar = new BoxedFontChar();
    this.skinEnabled = headEnabled;
    this.customModelDataEnabled = customModelDataEnabled;
    this.playerSkinModel = playerSkinModel;
  }

  @Getter
  private final String modelName;
  @Getter
  private final File textureFile;
  @Getter
  private final Material baseMaterial;
  @Getter
  private final int modelID;
  @Getter
  private final ModelData modelData;
  @Getter
  private final FontMeta fontMeta;
  @Getter
  private final BoxedFontChar boxedFontChar;
  @Getter
  private final boolean skinEnabled;
  @Getter
  private final boolean customModelDataEnabled;
  @Getter
  private final boolean playerSkinModel;
  @Getter
  @Setter
  private Skin skin;
  @Getter
  private GameProfile gameProfile;
  private ItemStack head;
  private ItemStack item;
  @Getter
  @Setter
  private File customModelFile = null;

  private void initProfile() {
    if (this.gameProfile == null && this.skin != null) {
      this.gameProfile = new GameProfile(this.skin.data.uuid, this.skin.name);
      this.gameProfile.getProperties()
              .put("textures", new Property("textures", this.skin.data.texture.value, this.skin.data.texture.signature));
    }
  }

  public void applySkinTo(final Player player) {
    final PlayerProfile profile = player.getPlayerProfile();
    profile.removeProperty("textures");
    profile.setProperty(new ProfileProperty("textures", this.skin.data.texture.value, this.skin.data.texture.signature));
    player.setPlayerProfile(profile);
  }

  public static TextureModel ofItemStack(ItemStack itemStack) {
    if (itemStack == null) {
      return null;
    }
    ItemMeta meta = itemStack.getItemMeta();
    if (meta == null) {
      return null;
    }
    PersistentDataContainer container = meta.getPersistentDataContainer();
    String modelId = container.get(Objects.requireNonNull(NamespaceFactory.provide("model-id")), PersistentDataType.STRING);
    if (modelId == null) {
      return null;
    }
    return TextureModel.valueOf(modelId);
  }

  public void applySkinTo(final TabLine line) {
    line.setTexture(this.skin.data.texture.value, this.skin.data.texture.signature);
  }

  public char getChar() {
    return this.boxedFontChar.getAsCharacter();
  }

  public ItemStack getItem() {
    if (this.item == null) {
      this.item = new ItemBuilder(this.baseMaterial)
              .modelData(this.modelID)
              .name(this.modelName)
              .addPersistentData("model-id", PersistentDataType.STRING, this.modelName)
              .build();
    }
    return this.item.clone();
  }

  public ItemStack getHead() {
    if (this.head != null) {
      return this.head.clone();
    }
    this.initProfile();

    this.head = UtilItem.produceHead(this.gameProfile);

    final ItemMeta meta = this.head.getItemMeta();
    final PersistentDataContainer container = meta.getPersistentDataContainer();
    container.set(NamespaceFactory.provide("Model"), PersistentDataType.STRING, this.modelName);
    this.head.setItemMeta(meta);
    return this.head.clone();
  }

}
