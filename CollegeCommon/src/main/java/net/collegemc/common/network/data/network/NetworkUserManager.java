package net.collegemc.common.network.data.network;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.model.AutoSynchronizedGlobalDataMap;
import net.collegemc.common.model.DataDomainManager;
import net.collegemc.common.model.DataMapContext;
import net.collegemc.common.network.data.NetworkGsonSerializer;
import org.redisson.api.RedissonClient;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NetworkUserManager {

  public static final String NETWORK_USER_NAMESPACE = "network-user-data";

  private final AutoSynchronizedGlobalDataMap<UUID, NetworkUserData> userDataMap;

  public NetworkUserManager(RedissonClient redissonClient, MongoClient mongoClient) {
    MongoDatabase mongoDatabase = mongoClient.getDatabase(GlobalGateway.DATABASE_NAME);
    DataMapContext<UUID, NetworkUserData> dataMapContext = DataMapContext.<UUID, NetworkUserData>mapContextBuilder()
            .creator(NetworkUserData::new)
            .redissonClient(redissonClient)
            .keyClass(UUID.class)
            .mongoDatabase(mongoDatabase)
            .namespace(NETWORK_USER_NAMESPACE)
            .serializer(new NetworkGsonSerializer())
            .valueClass(NetworkUserData.class)
            .build();

    DataDomainManager domainManager = GlobalGateway.getDataDomainManager();
    this.userDataMap = domainManager.getOrCreateAutoSyncDataDomain(dataMapContext);
  }

  public CompletableFuture<Void> cache(UUID userId) {
    return this.userDataMap.enableLocalCacheAsyncFor(userId);
  }

  public void uncache(UUID userId) {
    this.userDataMap.disableLocalCacheFor(userId);
  }

  public NetworkUserData getLocalCopy(UUID userId) {
    return this.userDataMap.getCachedValue(userId);
  }

  public void applyToRemoteUser(UUID userId, Consumer<NetworkUserData> consumer) {
    this.userDataMap.applyToData(userId, consumer);
  }

}
