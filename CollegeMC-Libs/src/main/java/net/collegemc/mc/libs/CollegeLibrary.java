package net.collegemc.mc.libs;

import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.Multimap;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoDatabase;
import lombok.Getter;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.database.mongodb.MongoDriverProperties;
import net.collegemc.common.database.redis.RedisGsonCodec;
import net.collegemc.mc.libs.actionbar.ActionBarManager;
import net.collegemc.mc.libs.blockdata.BlockDataManager;
import net.collegemc.mc.libs.displaywidgets.DisplayWidgetManager;
import net.collegemc.mc.libs.gui.GuiManager;
import net.collegemc.mc.libs.holograms.HologramManager;
import net.collegemc.mc.libs.holograms.implementations.nms.NMSHologramFactory;
import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.nametag.NameTagManager;
import net.collegemc.mc.libs.npcs.NPCManager;
import net.collegemc.mc.libs.npcs.abstraction.NPC;
import net.collegemc.mc.libs.npcs.serializer.MultiMapInstanceCreator;
import net.collegemc.mc.libs.npcs.serializer.OfflinePlayerSerializer;
import net.collegemc.mc.libs.npcs.serializer.ServerPlayerSerializer;
import net.collegemc.mc.libs.protocol.ProtocolListener;
import net.collegemc.mc.libs.regions.AbstractRegion;
import net.collegemc.mc.libs.regions.RegionManager;
import net.collegemc.mc.libs.regions.serializer.PolygonSerializer;
import net.collegemc.mc.libs.selectionmenu.SelectionMenuManager;
import net.collegemc.mc.libs.skinclient.PlayerSkinManager;
import net.collegemc.mc.libs.spigot.NameGenerator;
import net.collegemc.mc.libs.spigot.UtilChunk;
import net.collegemc.mc.libs.tablist.TabListManager;
import net.collegemc.mc.libs.tablist.implementation.EmptyTabList;
import net.collegemc.mc.libs.tokenclick.TokenActionManager;
import net.minecraft.server.level.ServerPlayer;
import org.bson.UuidRepresentation;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.awt.Polygon;
import java.io.File;
import java.util.Optional;

public class CollegeLibrary extends JavaPlugin {

  @Getter
  private static String serverName;
  @Getter
  private static CollegeLibrary instance;
  @Getter
  private static PaperCommandManager commandManager;
  @Getter
  private static ActionBarManager actionBarManager;
  @Getter
  private static BlockDataManager blockDataManager;
  @Getter
  private static HologramManager hologramManager;
  @Getter
  private static TabListManager tabListManager;
  @Getter
  private static TokenActionManager tokenActionManager;
  @Getter
  private static PlayerSkinManager playerSkinManager;
  @Getter
  private static GuiManager guiManager;
  @Getter
  private static NPCManager npcManager;
  @Getter
  private static RegionManager regionManager;
  @Getter
  private static SelectionMenuManager selectionMenuManager;
  @Getter
  private static GsonSerializer gsonSerializer;
  @Getter
  private static NameGenerator nameGenerator;
  @Getter
  private static NameTagManager nameTagManager;
  @Getter
  private static DisplayWidgetManager displayWidgetManager;
  private ServerConfigurationService coreConfigurationService;

  public static MongoDatabase getServerDatabase() {
    return GlobalGateway.getMongoClient().getDatabase(getServerDatabaseName());
  }

  public static String getServerDatabaseName() {
    return GlobalGateway.DATABASE_NAME + "-" + serverName;
  }

  private ServerConfigurationService coreConfigurationService;

  @Override
  public void onEnable() {
    instance = this;

    if (!this.fetchConfigurationService()) {
      return;
    }

    serverName = this.coreConfigurationService.getServerName();
    gsonSerializer = this.coreConfigurationService.getSerializer();
    this.injectLibrarySerialisation(gsonSerializer);

    this.saveResource("names.json", true);
    nameGenerator = new NameGenerator(new File(this.getDataFolder(), "names.json"));

    this.setupGateway();
    Msg.setServerPrefix(this.coreConfigurationService.getMessagePrefix());
    commandManager = new PaperCommandManager(this);
    actionBarManager = new ActionBarManager(this);
    blockDataManager = new BlockDataManager(this);
    nameTagManager = new NameTagManager();
    hologramManager = new HologramManager(new NMSHologramFactory());
    tabListManager = new TabListManager(this, player -> new EmptyTabList());
    tokenActionManager = new TokenActionManager(this, commandManager);
    playerSkinManager = new PlayerSkinManager();
    guiManager = new GuiManager(this);
    npcManager = new NPCManager(this);
    regionManager = new RegionManager(this);
    selectionMenuManager = new SelectionMenuManager(this);
    displayWidgetManager = new DisplayWidgetManager(this);

    Bukkit.getPluginManager().registerEvents(new UtilChunk.ChunkTrackListener(), this);
    Bukkit.getPluginManager().registerEvents(new ProtocolListener(), this);

    //this.setupResourcepack();
  }

  @Override
  public void onDisable() {
    npcManager.flush();
    regionManager.flush();
    Optional.ofNullable(blockDataManager).ifPresent(BlockDataManager::terminate);
  }

  private void setupGateway() {
    Config redissonConfig = new Config();

    RedisGsonCodec codec = new RedisGsonCodec(this.coreConfigurationService.getSerializer());
    redissonConfig.setCodec(codec);
    redissonConfig.setNettyThreads(this.coreConfigurationService.getRedisNettyThreads());

    SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
    singleServerConfig.setRetryInterval(this.coreConfigurationService.getRedisRetryInterval());
    singleServerConfig.setTimeout(this.coreConfigurationService.getRedisTimeout());
    singleServerConfig.setConnectionPoolSize(this.coreConfigurationService.getRedisConnectionPoolSize());
    singleServerConfig.setAddress(this.coreConfigurationService.getRedisAddress());

    GlobalGateway.initializeRedis(redissonConfig);

    MongoDriverProperties properties = this.coreConfigurationService.getMongoDriverProperties();
    String connectionString = "mongodb://%s:%s@%s:%s".formatted(
            properties.getUser(),
            properties.getPassword(),
            properties.getHostAddress(),
            properties.getHostPort()
    );

    MongoClientSettings mongoSettings = MongoClientSettings.builder()
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyConnectionString(new ConnectionString(connectionString))
            .build();

    GlobalGateway.initializeMongodb(mongoSettings);
  }

  private boolean fetchConfigurationService() {
    RegisteredServiceProvider<ServerConfigurationService> configServiceProvider = Bukkit.getServicesManager().getRegistration(ServerConfigurationService.class);
    if (configServiceProvider == null) {
      new IllegalStateException("No configuration service is provided.").printStackTrace();
      Bukkit.shutdown();
      return false;
    }
    this.coreConfigurationService = configServiceProvider.getProvider();
    return true;
  }

  private void injectLibrarySerialisation(GsonSerializer serializer) {
    serializer.registerTypeAdapter(Polygon.class, new PolygonSerializer());
    serializer.registerAbstractTypeHierarchyAdapter(NPC.class);
    serializer.registerAbstractTypeHierarchyAdapter(AbstractRegion.class);
    serializer.registerTypeHierarchyAdapter(OfflinePlayer.class, new OfflinePlayerSerializer());
    serializer.registerTypeHierarchyAdapter(ServerPlayer.class, new ServerPlayerSerializer());
    serializer.registerInstanceCreator(Multimap.class, new MultiMapInstanceCreator());
    serializer.setLoggingEnabled(false);
  }

}
