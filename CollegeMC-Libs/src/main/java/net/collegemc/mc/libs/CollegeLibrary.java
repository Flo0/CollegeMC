package net.collegemc.mc.libs;

import co.aikar.commands.PaperCommandManager;
import com.google.common.base.Preconditions;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import lombok.Getter;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.mongodb.MongoDriverProperties;
import net.collegemc.common.redis.RedisGsonCodec;
import net.collegemc.mc.libs.actionbar.ActionBarManager;
import net.collegemc.mc.libs.blockdata.BlockDataManager;
import net.collegemc.mc.libs.gui.GuiManager;
import net.collegemc.mc.libs.holograms.HologramManager;
import net.collegemc.mc.libs.holograms.implementations.protocollib.PlibHologramFactory;
import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.resourcepack.assembly.BlockModel;
import net.collegemc.mc.libs.resourcepack.assembly.CustomSound;
import net.collegemc.mc.libs.resourcepack.assembly.TextureModel;
import net.collegemc.mc.libs.resourcepack.distribution.ResourcepackManager;
import net.collegemc.mc.libs.skinclient.PlayerSkinManager;
import net.collegemc.mc.libs.spigot.UtilChunk;
import net.collegemc.mc.libs.tablist.TabListManager;
import net.collegemc.mc.libs.tablist.implementation.EmptyTabList;
import net.collegemc.mc.libs.tokenclick.TokenActionManager;
import org.bson.UuidRepresentation;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.util.Optional;

public class CollegeLibrary extends JavaPlugin {

  public static final String CORE_DB = "college-core";

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
  private static ResourcepackManager resourcepackManager;
  @Getter
  private static GuiManager guiManager;

  private ServerConfigurationService coreConfigurationService;

  @Override
  public void onEnable() {
    instance = this;

    this.fetchConfigurationService();
    this.setupGateway();
    Msg.setServerPrefix(this.coreConfigurationService.getMessagePrefix());

    commandManager = new PaperCommandManager(this);
    actionBarManager = new ActionBarManager(this);
    blockDataManager = new BlockDataManager(this);
    hologramManager = new HologramManager(new PlibHologramFactory());
    tabListManager = new TabListManager(this, player -> new EmptyTabList());
    tokenActionManager = new TokenActionManager(this, commandManager);
    playerSkinManager = new PlayerSkinManager(new GsonSerializer());
    guiManager = new GuiManager(this);

    Bukkit.getPluginManager().registerEvents(new UtilChunk.ChunkTrackListener(), this);


    this.setupResourcepack();
  }

  @Override
  public void onDisable() {
    Optional.ofNullable(blockDataManager).ifPresent(BlockDataManager::terminate);
    if (this.coreConfigurationService.isResourcepackEnabled() && resourcepackManager != null) {
      resourcepackManager.shutdown();
    }
  }

  private void setupResourcepack() {
    if (this.coreConfigurationService.isResourcepackEnabled()) {

      this.coreConfigurationService.getTextureModels().forEach(TextureModel::register);
      this.coreConfigurationService.getBlockModels().forEach(BlockModel::register);
      this.coreConfigurationService.getCustomSounds().forEach(CustomSound::register);

      String host = this.coreConfigurationService.resourcepackServerHost();
      int port = this.coreConfigurationService.resourcepackServerPort();
      resourcepackManager = new ResourcepackManager(host, port);
      if (!resourcepackManager.zipResourcepack(this, this.coreConfigurationService.getRawResourcepackFiles())) {
        Bukkit.shutdown();
      }
      resourcepackManager.startServer();
    }
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

  private void fetchConfigurationService() {
    RegisteredServiceProvider<ServerConfigurationService> configServiceProvider = Bukkit.getServicesManager().getRegistration(ServerConfigurationService.class);
    Preconditions.checkState(configServiceProvider != null, "No configuration service is provided.");
    this.coreConfigurationService = configServiceProvider.getProvider();
  }

}
