package net.collegemc.common.network.data;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.model.AutoSynchronizedGlobalDataMap;
import net.collegemc.common.model.DataDomainManager;
import net.collegemc.common.model.DataMapContext;
import org.redisson.api.RedissonClient;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NetworkUserManager {

  public static final String NETWORK_USER_NAMESPACE = "network-user-data";

  private final AutoSynchronizedGlobalDataMap<UUID, NetworkUser> userDataMap;

  public NetworkUserManager(RedissonClient redissonClient, MongoClient mongoClient) {
    MongoDatabase mongoDatabase = mongoClient.getDatabase(GlobalGateway.DATABASE_NAME);
    DataMapContext<UUID, NetworkUser> dataMapContext = DataMapContext.<UUID, NetworkUser>mapContextBuilder()
            .creator(NetworkUser::new)
            .redissonClient(redissonClient)
            .keyClass(UUID.class)
            .mongoDatabase(mongoDatabase)
            .namespace(NETWORK_USER_NAMESPACE)
            .serializer(new NetworkGsonSerializer())
            .valueClass(NetworkUser.class)
            .build();

    DataDomainManager domainManager = GlobalGateway.getDataDomainManager();
    this.userDataMap = domainManager.getOrCreateAutoSyncDataDomain(dataMapContext);
  }

  public CompletableFuture<Void> cache(UUID userId) {
    return userDataMap.enableLocalCacheAsyncFor(userId);
  }

  public void uncache(UUID userId) {
    userDataMap.disableLocalCacheFor(userId);
  }

  public NetworkUser getLocalCopy(UUID userId) {
    return userDataMap.getCachedValue(userId);
  }

  public void applyToRemoteUser(UUID userId, Consumer<NetworkUser> consumer) {
    userDataMap.applyToData(userId, consumer);
  }


}
