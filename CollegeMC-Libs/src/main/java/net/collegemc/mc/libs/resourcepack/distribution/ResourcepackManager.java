package net.collegemc.mc.libs.resourcepack.distribution;


import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.resourcepack.assembly.AssetLibrary;
import net.collegemc.mc.libs.resourcepack.assembly.ResourcepackAssembler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ResourcepackManager {

  public static final long SERVER_TIMESTAMP = System.currentTimeMillis();
  public static final String RESOURCEPACK_FILE_NAME = "serverpack.zip";

  private final int port;
  private final String host;
  private final File pack;
  private final String fileLocation = "/" + ResourcepackManager.SERVER_TIMESTAMP + "/" + ResourcepackManager.RESOURCEPACK_FILE_NAME;

  private PlayerBoundResourcePackServer server;

  public ResourcepackManager(String host, int port) {
    CollegeLibrary plugin = CollegeLibrary.getInstance();
    this.port = port;
    this.host = host;
    final File stampFolder = new File(plugin.getDataFolder() + File.separator + ResourcepackManager.SERVER_TIMESTAMP);
    this.pack = new File(stampFolder, ResourcepackManager.RESOURCEPACK_FILE_NAME);
    Bukkit.getPluginManager().registerEvents(new ResourcePackListener(this), plugin);
  }

  public void startServer() {
    this.startServer(CollegeLibrary.getInstance());
  }

  public boolean zipResourcepack(JavaPlugin plugin, File rawInput) {
    AssetLibrary assetLibrary = new AssetLibrary(plugin);
    assetLibrary.saveCache();
    ResourcepackAssembler assembler = new ResourcepackAssembler(plugin, assetLibrary, rawInput);
    try {
      assembler.zipResourcepack();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public String getResourceHash() {
    return this.server.getFileHashChecksum();
  }

  public String getDownloadURL() {
    return this.server.getDownloadURL();
  }

  public void shutdown() {
    this.server.terminate();
  }

  private void startServer(final JavaPlugin plugin) {
    plugin.getLogger().info("Starting async HTTP ResourcePackServer");
    try {
      plugin.getLogger().info("Resourcepack file location: " + this.fileLocation);
      this.server = new PlayerBoundResourcePackServer(this.host, this.port, this.fileLocation, plugin.getLogger(), this.pack);
      this.server.start();
      plugin.getLogger().info("Successfully started the HTTP ResourcePackServer");
    } catch (final IOException ex) {
      plugin.getLogger().severe("Failed to start HTTP ResourcePackServer!");
      ex.printStackTrace();
    }
  }

}
