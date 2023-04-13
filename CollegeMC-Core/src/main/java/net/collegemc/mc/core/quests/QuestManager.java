package net.collegemc.mc.core.quests;

import net.collegemc.common.GlobalGateway;
import net.collegemc.common.model.DataMapContext;
import net.collegemc.common.model.LocalDataMap;
import net.collegemc.common.network.data.college.ProfileId;
import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class QuestManager {

  public static final String NAMESPACE = "Quests";

  private final LocalDataMap<UUID, QuestList> questHolderLocalDataMap;

  public QuestManager(JavaPlugin plugin) {
    DataMapContext<UUID, QuestList> context = DataMapContext.<UUID, QuestList>mapContextBuilder()
            .creator(key -> new QuestList(new ProfileId(key)))
            .namespace(NAMESPACE)
            .keyClass(UUID.class)
            .valueClass(QuestList.class)
            .mongoDatabase(GlobalGateway.getMongoClient().getDatabase(GlobalGateway.DATABASE_NAME))
            .redissonClient(GlobalGateway.getRedissonClient())
            .serializer(CollegeLibrary.getGsonSerializer())
            .build();

    this.questHolderLocalDataMap = GlobalGateway.getDataDomainManager().getOrCreateLocalDomain(context);
    Bukkit.getPluginManager().registerEvents(new QuestListener(), plugin);
  }

  public void cacheQuestList(ProfileId profileId) {
    this.questHolderLocalDataMap.loadDataSync(profileId.getUid());
  }

  public void uncacheQuestList(ProfileId profileId) {
    this.questHolderLocalDataMap.unloadDataSync(profileId.getUid());
  }

  public QuestList getQuestList(ProfileId profileId) {
    return this.questHolderLocalDataMap.getData(profileId.getUid());
  }

}
