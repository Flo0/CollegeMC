package net.collegemc.velocity;

import com.google.inject.Inject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.SneakyThrows;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.database.mongodb.MongoDriverProperties;
import net.collegemc.common.network.proxy.ProxyTransferRequest;
import net.collegemc.common.database.redis.RedisGsonCodec;
import org.bson.UuidRepresentation;
import org.redisson.api.listener.MessageListener;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Plugin(id = "collegemc_proxy", name = "CollegeProxy", version = "1.0.0-SNAPSHOT", description = "Proxy for CollegeMC", authors = {"Gestankbratwurst"})
public class CollegeVelocity implements MessageListener<ProxyTransferRequest> {

  private final ProxyServer server;
  private final Logger logger;
  private final Path dataDir;

  @Inject
  public CollegeVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
    this.server = server;
    this.logger = logger;
    this.dataDir = dataDirectory;

    logger.info("Started Epro velocity proxy");
  }

  @Subscribe
  @SneakyThrows
  public void onProxyInitialization(ProxyInitializeEvent event) {
    logger.info("Loading config files");
    ProxyConfig config = new ProxyConfig();
    Path configPath = Paths.get(dataDir.toString(), "config.json");
    if (configPath.toFile().exists()) {
      config = config.getSerializer().fromJson(Files.readString(configPath), ProxyConfig.class);
    }
    String json = config.getSerializer().pretty(config);
    Files.writeString(configPath, json);

    setupGateway(config);
    GlobalGateway.getRemoteEventManager().registerListener(ProxyTransferRequest.CHANNEL, ProxyTransferRequest.class, this);
  }

  private void setupGateway(ProxyConfig config) {
    logger.info("Initializing the EproGateway");
    Config redissonConfig = new Config();

    RedisGsonCodec codec = new RedisGsonCodec(config.getSerializer());
    redissonConfig.setCodec(codec);
    redissonConfig.setNettyThreads(config.getRedisNettyThreads());

    SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
    singleServerConfig.setRetryInterval(config.getRedisRetryInterval());
    singleServerConfig.setTimeout(config.getRedisTimeout());
    singleServerConfig.setConnectionPoolSize(config.getRedisConnectionPoolSize());
    singleServerConfig.setAddress(config.getRedisAddress());

    GlobalGateway.initializeRedis(redissonConfig);

    MongoDriverProperties properties = config.getMongoDriverProperties();
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

  @Override
  public void onMessage(CharSequence channel, ProxyTransferRequest msg) {
    Optional<Player> player = server.getPlayer(msg.getUserId());
    Optional<RegisteredServer> registeredServer = server.getServer(msg.getServer());

    logger.info("User transfer request: " + msg.getUserId() + " -> " + msg.getServer());

    if (player.isEmpty()) {
      logger.warn("Player was not found for transfer: " + msg.getUserId());
      return;
    }

    if (registeredServer.isEmpty()) {
      logger.warn("Server was not found for transfer: " + msg.getServer());
      return;
    }

    ConnectionRequestBuilder builder = player.get().createConnectionRequest(registeredServer.get());

    if (msg.isIndicated()) {
      builder.fireAndForget();
      logger.info("Connection request was fired. [Indicator]");
    } else {
      builder.connectWithIndication();
      logger.info("Connection request was fired. [Indicator]");
    }
  }

}
