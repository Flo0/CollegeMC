package net.collegemc.mc.libs.resourcepack.assembly;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import lombok.Getter;
import net.collegemc.common.utils.ResourceCopy;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.resourcepack.distribution.ResourcepackManager;
import net.collegemc.mc.libs.skinclient.PlayerSkinManager;
import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcepackAssembler {

  private static final int META_FORMAT = 8;
  private static final char FONT_START = (char) 0x2F00;
  private static final String META_DESC = "- -";
  private static Logger LOGGER;

  public ResourcepackAssembler(JavaPlugin plugin, AssetLibrary assetLibrary, File rawInput) {
    LOGGER = plugin.getLogger();
    this.plugin = plugin;
    this.rawInput = rawInput;
    this.clearPluginFolder(plugin);
    this.playerSkinManager = CollegeLibrary.getPlayerSkinManager();
    this.packFolderSet = new ObjectLinkedOpenHashSet<>();
    this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    this.assetLibrary = assetLibrary;

    this.packFolderSet.add(this.packFolder = new File(plugin.getDataFolder() + File.separator + "resourcepack"));
    this.packFolderSet.add(this.assetFolder = new File(this.packFolder + File.separator + "assets"));
    this.packFolderSet.add(this.minecraftFolder = new File(this.assetFolder + File.separator + "minecraft"));
    this.packFolderSet.add(this.blockStateFolder = new File(this.minecraftFolder + File.separator + "blockstates"));
    this.packFolderSet.add(this.fontFolder = new File(this.minecraftFolder + File.separator + "font"));
    this.packFolderSet.add(this.langFolder = new File(this.minecraftFolder + File.separator + "lang"));
    this.packFolderSet.add(this.modelsFolder = new File(this.minecraftFolder + File.separator + "models"));
    this.packFolderSet.add(this.itemModelFolder = new File(this.modelsFolder + File.separator + "item"));
    this.packFolderSet.add(this.blockModelFolder = new File(this.modelsFolder + File.separator + "block"));
    this.packFolderSet.add(this.particlesFolder = new File(this.minecraftFolder + File.separator + "particles"));
    this.packFolderSet.add(this.soundsFolder = new File(this.minecraftFolder + File.separator + "sounds" + File.separator + "custom"));
    this.packFolderSet.add(this.texturesFolder = new File(this.minecraftFolder + File.separator + "textures"));
    this.mcmetaFile = new File(this.packFolder, "pack.mcmeta");
    this.soundsFile = new File(this.minecraftFolder, "sounds.json");
    final File stampFolder = new File(plugin.getDataFolder() + File.separator + ResourcepackManager.SERVER_TIMESTAMP);
    stampFolder.mkdirs();

    this.resourceZipFile = new File(stampFolder, "serverpack.zip");
  }

  private final ObjectLinkedOpenHashSet<File> packFolderSet;
  private final JavaPlugin plugin;
  private final PlayerSkinManager playerSkinManager;
  private final Gson gson;
  @Getter
  private final AssetLibrary assetLibrary;

  private final File resourceZipFile;
  private final File rawInput;

  private final File packFolder;
  private final File assetFolder;
  private final File minecraftFolder;
  private final File blockStateFolder;
  private final File fontFolder;
  private final File langFolder;
  private final File modelsFolder;
  private final File itemModelFolder;
  private final File blockModelFolder;
  private final File particlesFolder;
  private final File soundsFolder;
  private final File texturesFolder;

  private final File mcmetaFile;
  private final File soundsFile;


  private void clearPluginFolder(final JavaPlugin plugin) {
    for (final File folder : Objects.requireNonNull(plugin.getDataFolder().listFiles())) {
      if (folder.isDirectory()) {
        final String folderName = folder.getName();
        if (folderName.contains("temp") || folderName.matches("[0-9]+")) {
          try {
            FileUtils.deleteDirectory(folder);
          } catch (final IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
  }


  public void zipResourcepack() throws IOException {

    this.setupBaseDirectories();

    this.createMetaFile();

    this.compileModels();

    this.copyModelEngineModels();

    final FileOutputStream fos = new FileOutputStream(this.resourceZipFile);
    final ZipOutputStream zos = new ZipOutputStream(fos);
    this.zipFile(this.assetFolder, this.assetFolder.getName(), zos);
    this.zipFile(this.mcmetaFile, this.mcmetaFile.getName(), zos);

    zos.close();
    fos.close();
  }


  private void setupBaseDirectories() throws IOException {
    FileUtils.deleteDirectory(this.assetFolder);
    for (final File folder : this.packFolderSet) {
      folder.mkdirs();
    }
  }


  private void createMetaFile() throws IOException {
    String json = new PackMeta(ResourcepackAssembler.META_FORMAT, ResourcepackAssembler.META_DESC).getAsJsonString();
    Files.writeString(this.mcmetaFile.toPath(), json);
  }


  private void compileModels() throws IOException {
    final JsonObject fontJson = new JsonObject();
    final JsonArray providerArray = new JsonArray();


    // Textures
    // this.exportData(tempFolder);

    // Blocks
    this.loadBlockModels();

    // Items
    this.loadItemModels(providerArray);

    // Skins
    this.createSkinData();

    // TTF
    // TODO fix ttf
    this.createTrueTypeFont(providerArray, fontJson);

    // Json models
    this.createModelJsonFiles(this.assetLibrary);

    // Sounds
    this.createSoundFiles();

    this.copyRawFolder();
  }


  private void exportData(final File tempFolder) {
    try {
      final ResourceCopy copy = new ResourceCopy();
      final JarFile jf = new JarFile(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
      copy.copyResourceDirectory(jf, "resourcepack", tempFolder);
    } catch (final IOException ex) {
      ex.printStackTrace();
    }
  }


  private void loadBlockModels() throws IOException {
    final Map<String, JsonObject> blockstateJsonMap = new HashMap<>();

    for (final BlockModel blockModel : BlockModel.values()) {
      final String vanillaName = blockModel.baseMaterial().getKey().getKey();
      final File modelBlockImage = blockModel.originalFile();
      if (!modelBlockImage.exists()) {
        this.plugin.getLogger().severe("Could not find image of " + blockModel.modelName());
        continue;
      }

      final JsonObject stateJson = blockstateJsonMap.computeIfAbsent(vanillaName, key -> new JsonObject());

      final JsonObject variantsJson;
      if (stateJson.has("variants")) {
        variantsJson = stateJson.get("variants").getAsJsonObject();
      } else {
        variantsJson = new JsonObject();
      }

      final JsonObject modelJson = new JsonObject();
      modelJson.addProperty("model", "block/" + blockModel.toString().toLowerCase());
      variantsJson.add(blockModel.blockStateApplicant(), modelJson);
      stateJson.add("variants", variantsJson);

      final JsonObject customModelJson = new JsonObject();
      customModelJson.addProperty("parent", "block/cube_all");
      final JsonObject textureJson = new JsonObject();
      textureJson.addProperty("all", blockModel.toString().toLowerCase());
      customModelJson.add("textures", textureJson);

      FileUtils.copyFile(modelBlockImage, new File(this.texturesFolder, blockModel.modelName().toLowerCase() + ".png"));
      String jsonStr = this.gson.toJson(customModelJson);
      Files.writeString(new File(this.blockModelFolder, blockModel.modelName().toLowerCase() + ".json").toPath(), jsonStr);
    }

    for (final Entry<String, JsonObject> entry : blockstateJsonMap.entrySet()) {
      final File stateFile = new File(this.blockStateFolder, entry.getKey() + ".json");
      final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(stateFile), StandardCharsets.UTF_8);
      osw.write(this.gson.toJson(entry.getValue()));
      osw.close();
    }
  }


  private void loadItemModels(final JsonArray providerArray) throws IOException {
    char fontIndex = FONT_START;

    for (TextureModel textureModel : TextureModel.values()) {
      if (textureModel.isCustomModelDataEnabled()) {
        continue;
      }

      File icon = textureModel.getTextureFile();

      final boolean isBlock = textureModel.getBaseMaterial().isBlock();
      final String nmsName = textureModel.getBaseMaterial().getKey().getKey();
      final File resourceTextureFolder = new File(this.texturesFolder + File.separator + nmsName);
      resourceTextureFolder.mkdirs();

      final File imageFile = new File(resourceTextureFolder, "" + textureModel.getModelID() + ".png");
      FileUtils.copyFile(icon, imageFile);

      final File modNMFolder = isBlock ? this.blockModelFolder : this.itemModelFolder;
      final File resourceModelFolder = new File(modNMFolder + File.separator + nmsName);
      resourceModelFolder.mkdirs();
      final File resourceModelFile = new File(resourceModelFolder, "" + textureModel.getModelID() + ".json");

      final String iconPath = "minecraft:" + nmsName + "/" + textureModel.getModelID() + ".png";
      textureModel.getBoxedFontChar().value = fontIndex;
      final FontMeta fontMeta = textureModel.getFontMeta();
      final JsonObject fontProvider = new JsonObject();
      fontProvider.addProperty("file", iconPath);
      final JsonArray charArray = new JsonArray();
      charArray.add(fontIndex);
      fontProvider.add("chars", charArray);
      fontProvider.addProperty("height", fontMeta.height());
      fontProvider.addProperty("ascent", fontMeta.ascent());
      fontProvider.addProperty("type", fontMeta.type());
      final int horizShift = fontMeta.horizontalShift();
      final int vertShift = fontMeta.verticalShift();
      if (horizShift != 0 && vertShift != 0) {
        final JsonArray shiftArray = new JsonArray();
        shiftArray.add(vertShift);
        shiftArray.add(horizShift);
        fontProvider.add("shift", shiftArray);
      }
      providerArray.add(fontProvider);
      fontIndex++;

      final ModelData modelData = textureModel.getModelData();
      if (modelData != null) {
        final JsonObject modelJson = new JsonObject();
        modelJson.addProperty("parent", textureModel.getModelData().modelParent());
        final JsonObject textureJson = new JsonObject();
        textureJson.addProperty("layer0", nmsName + "/" + textureModel.getModelID());
        textureJson.addProperty("particles", nmsName + "/" + textureModel.getModelID());
        modelJson.add("textures", textureJson);

        final JsonObject elementsJson = textureModel.getModelData().elementsJson();
        if (elementsJson != null) {
          modelJson.add("elements", elementsJson);
        }

        final JsonObject displayJson = textureModel.getModelData().displayJson();
        if (displayJson != null) {
          modelJson.add("display", displayJson);
        }

        final String data = this.gson.toJson(modelJson);
        final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(resourceModelFile));
        osw.write(data);
        osw.close();
      }
    }

    // Load custom models
    final File customtextureFolder = new File(this.texturesFolder + File.separator + "custom"); // minecraft/textures/custom
    final File customModelFolder = new File(this.itemModelFolder + File.separator + "custom"); // minecraft/models/items/custom
    // final File tempCustomModelFolder = new File(tempFolder + File.separator + "custommodel"); // intern
    // final File customTempModelFolder = new File(tempCustomModelFolder + File.separator + "models"); // intern
    // final File customTempTextureFolder = new File(tempCustomModelFolder + File.separator + "textures"); // intern
    // itemModelFolder

    if (!customtextureFolder.exists()) {
      customtextureFolder.mkdirs();
    }

    if (!customModelFolder.exists()) {
      customModelFolder.mkdirs();
    }

    // --- --- --- Copy all custom textures/models --- --- ---
    for (TextureModel textureModel : TextureModel.values()) {
      if (!textureModel.isCustomModelDataEnabled()) {
        continue;
      }
      File imageFile = textureModel.getTextureFile();
      File customDescFile = textureModel.getCustomModelFile();
      if (customDescFile == null) {
        this.plugin.getLogger().severe("Custom model without desc file: " + textureModel.getModelID());
        continue;
      }
      FileUtils.copyFile(imageFile, new File(customtextureFolder, imageFile.getName().toLowerCase()));
      FileUtils.copyFile(customDescFile, new File(customModelFolder, customDescFile.getName().toLowerCase()));
    }
  }


  private void copyRawFolder() {
    if (this.rawInput == null) {
      return;
    }
    try {
      FileUtils.copyDirectory(this.rawInput, this.minecraftFolder);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }


  private void createSoundFiles() {
    final JsonObject jsonObject = new JsonObject();
    try {
      for (CustomSound customSound : CustomSound.values()) {
        File soundFile = customSound.getSoundFile();
        String fileName = soundFile.getName();
        FileUtils.copyFile(customSound.getSoundFile(), new File(this.soundsFolder, fileName));
        final JsonObject soundJson = new JsonObject();
        final JsonArray soundList = new JsonArray();
        final String fileStrip = fileName.contains(".") ? fileName.split("\\.")[0] : fileName;
        soundList.add("custom/" + fileStrip);
        soundJson.add("sounds", soundList);
        jsonObject.add("custom." + fileStrip, soundJson);
      }
    } catch (final IOException e) {
      e.printStackTrace();
    }
    if (!jsonObject.entrySet().isEmpty()) {
      final String data = this.gson.toJson(jsonObject);
      try {
        Files.writeString(this.soundsFile.toPath(), data);
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }


  private void copyModelEngineModels() {
    final File pluginsFolder = this.plugin.getDataFolder().getParentFile();
    final File modelEngineFolder = new File(pluginsFolder + File.separator + "ModelEngine_Beta");
    if (!modelEngineFolder.exists()) {
      LOGGER.info("Cant find ModelEngine folder: " + modelEngineFolder);
      return;
    }
    final File modelEngineAssetsFolder = new File(modelEngineFolder + File.separator + "resource pack" + File.separator + "assets");
    if (!modelEngineAssetsFolder.exists()) {
      LOGGER.info("Cant find ModelEngine folder: " + modelEngineAssetsFolder);
      return;
    }

    try {
      FileUtils.copyDirectory(modelEngineAssetsFolder, this.assetFolder);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }


  private void createSkinData() {
    for (final TextureModel textureModel : TextureModel.values()) {
      if (textureModel.isSkinEnabled()) {
        final String name = "COLLEGE_MODEL_" + textureModel.getModelID();
        final File imageFile = textureModel.getTextureFile();
        this.playerSkinManager.requestNamedSkin(name, imageFile, !textureModel.isPlayerSkinModel(), textureModel::setSkin);
      }
    }
  }


  private void createTrueTypeFont(final JsonArray providerArray, final JsonObject fontJson) throws IOException {
    final JsonObject ttfProvider = new JsonObject();
    ttfProvider.addProperty("type", "ttf");
    ttfProvider.addProperty("size", 10);
    ttfProvider.addProperty("oversample", 4.5);
    final JsonArray shiftArray = new JsonArray();
    shiftArray.add(0);
    shiftArray.add(1);
    ttfProvider.add("shift", shiftArray);
    ttfProvider.addProperty("file", "minecraft:uniformcenter.ttf");
    // FIXME add ttf back
    // providerArray.add(ttfProvider);

    /*
    final File ttfFile = new File(tempFolder, "uniformcenter.ttf");
    FileUtils.copyFile(ttfFile, new File(this.fontFolder, "uniformcenter.ttf"));

    final File tempUnicodeFolder = new File(tempFolder + File.separator + "fontdata");
    final File unicodeFolder = new File(this.texturesFolder + File.separator + "font");
    unicodeFolder.mkdirs();

    for (final File unicodeImage : tempUnicodeFolder.listFiles()) {
      FileUtils.copyFile(unicodeImage, new File(unicodeFolder, unicodeImage.getName()));
    }
     */

    fontJson.add("providers", providerArray);
    final File fontFile = new File(this.fontFolder, "default.json");
    final OutputStreamWriter oswFont = new OutputStreamWriter(new FileOutputStream(fontFile), StandardCharsets.UTF_8);
    oswFont.write(this.gson.toJson(fontJson));
    oswFont.close();
  }


  private void createModelJsonFiles(final AssetLibrary assetLibrary) throws IOException {
    final Map<Material, JsonObject> cachedJsons = Maps.newHashMap();

    for (final TextureModel textureModel : TextureModel.values()) {
      final String nmsName = textureModel.getBaseMaterial().getKey().getKey();
      final boolean isBlock = textureModel.getBaseMaterial().isBlock();
      final JsonArray overrideArray;
      final JsonObject modelObject;

      if (textureModel.getModelData() == null) {
        continue;
      }

      if (!cachedJsons.containsKey(textureModel.getBaseMaterial())) {
        modelObject = new JsonObject();
        modelObject.addProperty("parent", assetLibrary.getAssetModelParent(nmsName));
        if (!isBlock) {
          final JsonObject textureObject = new JsonObject();
          textureObject.addProperty("layer0", assetLibrary.getAssetModelLayer0(nmsName));
          modelObject.add("textures", textureObject);
        }

        overrideArray = new JsonArray();
        modelObject.add("overrides", overrideArray);

      } else {
        modelObject = cachedJsons.get(textureModel.getBaseMaterial());
        overrideArray = modelObject.get("overrides").getAsJsonArray();
      }

      final JsonObject overrideObject = new JsonObject();
      final String customModelName;
      if (textureModel.isCustomModelDataEnabled()) {
        customModelName = assetLibrary.getAssetModelLayer0(nmsName).split("/")[0] + "/custom/" + textureModel.toString().toLowerCase();
      } else {
        customModelName = assetLibrary.getAssetModelLayer0(nmsName).split("/")[0] + "/" + nmsName + "/" + textureModel.getModelID();
      }
      overrideObject.addProperty("model", customModelName);
      final JsonObject predicateObject = new JsonObject();
      predicateObject.addProperty("custom_model_data", textureModel.getModelID());
      overrideObject.add("predicate", predicateObject);
      overrideArray.add(overrideObject);

      modelObject.add("overrides", overrideArray);
      cachedJsons.put(textureModel.getBaseMaterial(), modelObject);
    }

    for (final TextureModel textureModel : TextureModel.values()) {
      if (textureModel.getModelData() != null) {
        final File modelFolder = textureModel.getBaseMaterial().isBlock() ? this.blockModelFolder : this.itemModelFolder;
        final File modelFile = new File(modelFolder, textureModel.getBaseMaterial().getKey().getKey() + ".json");
        final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(modelFile));
        osw.write(this.gson.toJson(cachedJsons.get(textureModel.getBaseMaterial())));
        osw.close();
      }
    }
  }


  private void zipFile(final File fileToZip, final String fileName, final ZipOutputStream zipOut) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }
    if (fileToZip.isDirectory()) {
      if (fileName.endsWith("/")) {
        zipOut.putNextEntry(new ZipEntry(fileName));
        zipOut.closeEntry();
      } else {
        zipOut.putNextEntry(new ZipEntry(fileName + "/"));
        zipOut.closeEntry();
      }
      final File[] children = fileToZip.listFiles();
      for (final File childFile : children) {
        this.zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
      }
      return;
    }
    final FileInputStream fis = new FileInputStream(fileToZip);
    final ZipEntry zipEntry = new ZipEntry(fileName);
    zipOut.putNextEntry(zipEntry);
    final byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }
    fis.close();
  }


  private void await(final long ms) {
    try {
      Thread.sleep(ms);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
  }


}
