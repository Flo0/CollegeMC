package net.collegemc.common.bridge;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteEventManager {

  private final RedissonClient redissonClient;
  private final String mainTopicName;
  private final Map<String, RTopic> topicMap = new ConcurrentHashMap<>();

  public RemoteEventManager(String mainChannel, RedissonClient client) {
    this.redissonClient = client;
    this.mainTopicName = mainChannel;
  }

  @Contract("_, null -> fail; null, _ -> fail")
  public <M extends RemoteEvent> void registerListener(Class<M> clazz, MessageListener<? extends M> listener) {
    this.registerListener(this.mainTopicName, clazz, listener);
  }

  @Contract("_, _, null -> fail; _, null, _ -> fail; null, _, _ -> fail")
  public <M extends RemoteEvent> void registerListener(String topicName, Class<M> clazz, MessageListener<? extends M> listener) {
    Preconditions.checkArgument(clazz != null);
    Preconditions.checkArgument(listener != null);
    Preconditions.checkArgument(topicName != null);
    topicMap.computeIfAbsent(topicName, redissonClient::getTopic).addListener(clazz, listener);
  }

  public <M extends RemoteEvent> void callEvent(@NotNull M event) {
    this.callEvent(this.mainTopicName, event);
  }

  public <M extends RemoteEvent> void callEvent(@NotNull String topicName, @NotNull M event) {
    topicMap.computeIfAbsent(topicName, redissonClient::getTopic).publish(event);
  }

}
