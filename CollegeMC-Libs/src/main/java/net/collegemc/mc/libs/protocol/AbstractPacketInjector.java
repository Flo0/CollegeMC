package net.collegemc.mc.libs.protocol;

import net.collegemc.mc.libs.tasks.TaskManager;
import net.minecraft.network.protocol.Packet;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractPacketInjector<T> {


  private final Map<Class<?>, List<PacketHandler<T, Packet<?>>>> incomingPacketCallbacks = new HashMap<>();
  private final Map<Class<?>, List<PacketHandler<T, Packet<?>>>> outgoingPacketCallbacks = new HashMap<>();

  protected abstract String getPacketHandlerName();

  @SuppressWarnings("unchecked")
  public <P extends Packet<?>> void register(PacketHandler<T, P> packetHandler) {
    for (Direction direction : packetHandler.getDirections()) {
      switch (direction) {
        case INCOMING -> this.incomingPacketCallbacks
                .computeIfAbsent(packetHandler.getPacketType(), key -> new ArrayList<>())
                .add((PacketHandler<T, Packet<?>>) packetHandler);
        case OUTGOING -> this.outgoingPacketCallbacks
                .computeIfAbsent(packetHandler.getPacketType(), key -> new ArrayList<>())
                .add((PacketHandler<T, Packet<?>>) packetHandler);
      }
    }
  }

  public abstract void inject(T target) throws InvocationTargetException, IllegalAccessException;

  public abstract void uninject(T target);

  protected void triggerIncoming(T target, Object msg) {
    Class<?> type = msg.getClass();
    List<PacketHandler<T, Packet<?>>> callbacks = this.incomingPacketCallbacks.get(type);
    invokeCallbacks(target, msg, callbacks);
  }

  protected void triggerOutgoing(T target, Object msg) {
    Class<?> type = msg.getClass();
    List<PacketHandler<T, Packet<?>>> callbacks = this.outgoingPacketCallbacks.get(type);
    invokeCallbacks(target, msg, callbacks);
  }

  private void invokeCallbacks(T target, Object msg, List<PacketHandler<T, Packet<?>>> callbacks) {
    if (callbacks == null) {
      return;
    }
    callbacks.forEach(callback -> {
      if (callback.isAsync()) {
        TaskManager.runOnComputationPool(() -> callback.accept(target, callback.getPacketType().cast(msg)));
      } else {
        callback.accept(target, callback.getPacketType().cast(msg));
      }
    });
  }

  public enum Direction {
    INCOMING, OUTGOING
  }
}
