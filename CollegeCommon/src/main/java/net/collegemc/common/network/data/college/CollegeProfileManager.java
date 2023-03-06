package net.collegemc.common.network.data.college;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.model.DataDomainManager;
import net.collegemc.common.model.DataMapContext;
import net.collegemc.common.model.LocalDataMap;
import net.collegemc.common.network.data.NetworkGsonSerializer;
import org.redisson.api.RedissonClient;

import java.util.UUID;

public class CollegeProfileManager {

  public static final String COLLEGE_PROFILE_NAMESPACE = "college-profile-data";

  private final LocalDataMap<UUID, CollegeProfile> userDataMap;

  public CollegeProfileManager(RedissonClient redissonClient, MongoClient mongoClient) {
    MongoDatabase mongoDatabase = mongoClient.getDatabase(GlobalGateway.DATABASE_NAME);
    DataMapContext<UUID, CollegeProfile> dataMapContext = DataMapContext.<UUID, CollegeProfile>mapContextBuilder()
            .creator(CollegeProfile::new)
            .redissonClient(redissonClient)
            .keyClass(UUID.class)
            .mongoDatabase(mongoDatabase)
            .namespace(COLLEGE_PROFILE_NAMESPACE)
            .serializer(new NetworkGsonSerializer())
            .valueClass(CollegeProfile.class)
            .build();

    DataDomainManager domainManager = GlobalGateway.getDataDomainManager();
    this.userDataMap = domainManager.getOrCreateLocalDomain(dataMapContext);
  }

  public void load(UUID userId) {
    this.userDataMap.loadDataSync(userId);
  }

  public void unload(UUID userId) {
    this.userDataMap.loadDataSync(userId);
  }

  public CollegeProfile get(UUID userId) {
    return this.userDataMap.getData(userId);
  }

}
