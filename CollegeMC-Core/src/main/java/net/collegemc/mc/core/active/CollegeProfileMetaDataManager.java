package net.collegemc.mc.core.active;

import com.mongodb.client.MongoDatabase;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.model.DataMapContext;
import net.collegemc.common.model.LocalDataMap;
import net.collegemc.common.network.data.college.ProfileId;
import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CollegeProfileMetaDataManager {

  public static final String NAMESPACE = "Profile-Metadata";

  private final LocalDataMap<UUID, CollegeProfileMetaData> metaDataMap;

  public CollegeProfileMetaDataManager() {
    MongoDatabase mongoDatabase = GlobalGateway.getMongoClient().getDatabase(GlobalGateway.DATABASE_NAME);
    DataMapContext<UUID, CollegeProfileMetaData> metaDataContext = DataMapContext.<UUID, CollegeProfileMetaData>mapContextBuilder()
            .keyClass(UUID.class)
            .valueClass(CollegeProfileMetaData.class)
            .creator(key -> new CollegeProfileMetaData())
            .redissonClient(GlobalGateway.getRedissonClient())
            .mongoDatabase(mongoDatabase)
            .serializer(CollegeLibrary.getGsonSerializer())
            .namespace(NAMESPACE)
            .build();
    this.metaDataMap = GlobalGateway.getDataDomainManager().getOrCreateLocalDomain(metaDataContext);
  }

  public void loadMetaData(ProfileId profileId) {
    this.metaDataMap.loadDataSync(profileId.getUid());
  }

  public CollegeProfileMetaData getMetaData(ProfileId profileId) {
    return this.metaDataMap.getData(profileId.getUid());
  }

  public void unloadMetaData(ProfileId profileId) {
    this.metaDataMap.unloadDataSync(profileId.getUid());
  }

  public void writeMeta(Player player, ProfileId profileId) {
    CollegeProfileMetaData metaData = this.getMetaData(profileId);
    if (metaData == null) {
      return;
    }
    metaData.setLastKnownLocation(player.getLocation());
    metaData.setInventoryContent(player.getInventory().getContents());
  }

}
