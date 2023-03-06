package net.collegemc.common.bridge;

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

  public <M extends RemoteEvent> void registerListener(Class<M> clazz, MessageListener<? extends M> listener) {
    this.registerListener(this.mainTopicName, clazz, listener);
  }

  public <M extends RemoteEvent> void registerListener(String topicName, Class<M> clazz, MessageListener<? extends M> listener) {
    topicMap.computeIfAbsent(topicName, redissonClient::getTopic).addListener(clazz, listener);
  }

  public <M extends RemoteEvent> void callEvent(M event) {
    this.callEvent(this.mainTopicName, event);
  }

  public <M extends RemoteEvent> void callEvent(String topicName, M event) {
    topicMap.computeIfAbsent(topicName, redissonClient::getTopic).publish(event);
  }

}
