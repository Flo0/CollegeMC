package net.collegemc.common.network.data.college;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.mineskin.data.Skin;
import net.collegemc.common.model.AutoSynchronizedGlobalDataMap;
import net.collegemc.common.model.DataDomainManager;
import net.collegemc.common.model.DataMapContext;
import org.bson.conversions.Bson;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CollegeProfileManager {

  public static final String COLLEGE_PROFILE_NAMESPACE = "College-Profiles";

  private final AutoSynchronizedGlobalDataMap<UUID, CollegeProfile> userDataMap;

  public CollegeProfileManager(RedissonClient redissonClient, MongoClient mongoClient) {
    MongoDatabase mongoDatabase = mongoClient.getDatabase(GlobalGateway.DATABASE_NAME);
    DataMapContext<UUID, CollegeProfile> dataMapContext = DataMapContext.<UUID, CollegeProfile>mapContextBuilder()
            .creator(key -> new CollegeProfile(new ProfileId(key)))
            .redissonClient(redissonClient)
            .keyClass(UUID.class)
            .mongoDatabase(mongoDatabase)
            .namespace(COLLEGE_PROFILE_NAMESPACE)
            .serializer(new GsonSerializer())
            .valueClass(CollegeProfile.class)
            .build();

    DataDomainManager domainManager = GlobalGateway.getDataDomainManager();
    this.userDataMap = domainManager.getOrCreateAutoSyncDataDomain(dataMapContext);
  }

  public void registerListener(String key, BiConsumer<UUID, CollegeProfile> listener) {
    this.userDataMap.registerListener(key, listener);
  }

  public Boolean nameExists(String name) {
    return this.userDataMap.getBackbone().query(collection -> {
      Bson filter = Filters.eq("name", name);
      Bson projection = Projections.include("name");
      return collection.find(filter).limit(1).projection(projection).map(CollegeProfile::getName);
    }, result -> result.first() != null);
  }

  public void load(ProfileId profileId) {
    this.userDataMap.enableLocalCacheFor(profileId.getUid());
  }

  public void unload(ProfileId profileId) {
    this.userDataMap.disableLocalCacheFor(profileId.getUid());
  }

  public CollegeProfile getLoaded(ProfileId profileId) {
    return this.userDataMap.getCachedValue(profileId.getUid());
  }

  public CollegeProfile createProfile(String name, UUID userId, boolean cache) {
    return createProfile(name, userId, null, cache);
  }

  public Skin requestSkin(ProfileId profileId) {
    CollegeProfile profile = this.getLoaded(profileId);
    if (profile == null) {
      return userDataMap.getBackbone().queryProperty(profileId.getUid(), "skin", Skin.class);
    }
    return profile.getSkin();
  }

  public CollegeProfile createProfile(String name, UUID userId, Skin skin, boolean cache) {
    ProfileId profileId = ProfileId.random();
    this.userDataMap.getOrCreateRealTimeData(profileId.getUid());
    this.userDataMap.applyToData(profileId.getUid(), profile -> {
      profile.setMinecraftUserId(userId);
      profile.setName(name);
      if (skin != null) {
        profile.setSkin(skin);
      }
    });
    if (cache) {
      this.load(profileId);
      return this.getLoaded(profileId);
    } else {
      return this.userDataMap.getOrCreateRealTimeData(profileId.getUid());
    }
  }

  public void applyToProfile(ProfileId profileId, Consumer<CollegeProfile> consumer) {
    this.userDataMap.applyToData(profileId.getUid(), consumer);
  }

  public ProfileId getIdByName(String name) {
    return this.userDataMap.getBackbone().query(collection -> {
      Bson filter = Filters.eq("name", name);
      Bson projection = Projections.include("collegeProfileId");
      return collection.find(filter).projection(projection).map(CollegeProfile::getCollegeProfileId);
    }, MongoIterable::first);
  }

  public List<String> getAllNames(Collection<ProfileId> profileIds) {
    return this.userDataMap.getBackbone().query(collection -> {
      Bson filter = Filters.in("collegeProfileId", profileIds);
      Bson projection = Projections.include("name");
      return collection.find(filter).projection(projection).map(CollegeProfile::getName);
    }, result -> {
      List<String> names = new ArrayList<>();
      result.into(names);
      return names;
    });
  }

  public CollegeProfile requestProfile(ProfileId friendId) {
    CollegeProfile profile = this.getLoaded(friendId);
    if (profile == null) {
      profile = this.userDataMap.getOrCreateRealTimeData(friendId.getUid());
    }
    return profile;
  }
}
