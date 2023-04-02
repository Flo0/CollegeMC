package net.collegemc.mc.core.friends;

import com.mongodb.client.MongoDatabase;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.model.AutoSynchronizedGlobalDataMap;
import net.collegemc.common.model.DataMapContext;
import net.collegemc.common.model.GlobalDataMap;
import net.collegemc.common.network.data.college.ProfileId;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.tasks.TaskManager;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FriendsManager {

  public static final String LIST_NAMESPACE = "Friends";
  public static final String REQUEST_NAMESPACE = "Friend-Requests";

  private final AutoSynchronizedGlobalDataMap<UUID, FriendsList> friendsLists;
  private final GlobalDataMap<UUID, FriendRequests> friendRequests;

  public FriendsManager() {
    MongoDatabase mongoDatabase = GlobalGateway.getMongoClient().getDatabase(GlobalGateway.DATABASE_NAME);
    DataMapContext<UUID, FriendsList> listContext = DataMapContext.<UUID, FriendsList>mapContextBuilder()
            .keyClass(UUID.class)
            .valueClass(FriendsList.class)
            .creator(key -> new FriendsList(new ProfileId(key)))
            .namespace(LIST_NAMESPACE)
            .serializer(CollegeLibrary.getGsonSerializer())
            .redissonClient(GlobalGateway.getRedissonClient())
            .mongoDatabase(mongoDatabase)
            .build();
    DataMapContext<UUID, FriendRequests> requestsContext = DataMapContext.<UUID, FriendRequests>mapContextBuilder()
            .keyClass(UUID.class)
            .valueClass(FriendRequests.class)
            .creator(key -> new FriendRequests())
            .namespace(REQUEST_NAMESPACE)
            .serializer(CollegeLibrary.getGsonSerializer())
            .redissonClient(GlobalGateway.getRedissonClient())
            .mongoDatabase(mongoDatabase)
            .build();
    this.friendsLists = GlobalGateway.getDataDomainManager().getOrCreateAutoSyncDataDomain(listContext);
    this.friendRequests = GlobalGateway.getDataDomainManager().getOrCreateGlobalDomain(requestsContext);
  }

  public FriendsList fetchRemoteList(ProfileId profileId) {
    return this.friendsLists.getOrCreateRealTimeData(profileId.getUid());
  }

  public FriendsList getActiveFriendsList(ProfileId profileId) {
    return this.friendsLists.getCachedValue(profileId.getUid());
  }

  public void loadFriendsList(ProfileId profileId) {
    this.friendsLists.enableLocalCacheFor(profileId.getUid());
  }

  public void unloadFriendsList(ProfileId profileId) {
    this.friendsLists.disableLocalCacheFor(profileId.getUid());
  }

  public CompletableFuture<Boolean> hasSentFriendRequest(ProfileId sender, ProfileId receiver) {
    return TaskManager.supplyOnIOPool(() -> this.friendRequests.getOrCreateRealTimeData(sender.getUid())).thenApply(list -> list.hasSentTo(receiver));
  }

  public CompletableFuture<Boolean> hasReceivedFriendRequest(ProfileId sender, ProfileId receiver) {
    return TaskManager.supplyOnIOPool(() -> this.friendRequests.getOrCreateRealTimeData(receiver.getUid())).thenApply(list -> list.hasReceivedFrom(sender));
  }

  public CompletableFuture<Void> removeFriend(ProfileId sender, ProfileId receiver) {
    return TaskManager.runOnIOPool(() -> this.friendsLists.applyToBoth(sender.getUid(), receiver.getUid(), (senderList, receiverList) -> {
      senderList.removeFriend(receiver);
      receiverList.removeFriend(sender);
    }));
  }

  public CompletableFuture<Void> sendFriendRequest(ProfileId sender, ProfileId receiver) {
    return TaskManager.runOnIOPool(() -> this.friendRequests.applyToBoth(sender.getUid(), receiver.getUid(), (senderReq, receiverReq) -> {
      senderReq.addSentTo(receiver);
      receiverReq.addReceivedFrom(sender);
    }));
  }

  public CompletableFuture<Void> solveFriendRequest(ProfileId sender, ProfileId receiver, boolean accepted) {
    return TaskManager.runOnIOPool(() -> this.friendRequests.applyToBoth(sender.getUid(), receiver.getUid(), (senderReq, receiverReq) -> {
      senderReq.removeSentTo(sender);
      receiverReq.removeReceivedFrom(receiver);
    })).thenRun(() -> {
      if (accepted) {
        this.friendsLists.applyToBoth(sender.getUid(), receiver.getUid(), (senderList, receiverList) -> {
          senderList.addFriend(receiver);
          receiverList.addFriend(sender);
        });
      }
    });
  }

}
