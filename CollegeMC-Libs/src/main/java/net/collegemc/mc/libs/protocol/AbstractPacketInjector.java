package net.collegemc.mc.libs.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import net.minecraft.network.protocol.Packet;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractPacketInjector<T> extends ChannelInitializer<Channel> {

  private final Map<Class<?>, List<Consumer<Object>>> incomingPacketCallbacks = new HashMap<>();
  private final Map<Class<?>, List<Consumer<Object>>> outgoingPacketCallbacks = new HashMap<>();

  private static final String CHANNEL_OUT = "HandlerOutgoing";
  private static final String CHANNEL_IN = "HandlerIncoming";

  protected abstract String getPacketDecoderName();

  protected abstract String getPacketEncoderName();

  protected abstract ChannelHandler channelOut();

  protected abstract ChannelHandler channelIn();

  public <P extends Packet<?>> void register(Consumer<P> consumer, Class<P> type, Direction... directions) {
    for (Direction direction : directions) {
      switch (direction) {
        case INCOMING -> this.incomingPacketCallbacks
                .computeIfAbsent(type, key -> new ArrayList<>()).add(x -> consumer.accept(type.cast(x)));
        case OUTGOING -> this.outgoingPacketCallbacks
                .computeIfAbsent(type, key -> new ArrayList<>()).add(x -> consumer.accept(type.cast(x)));
      }
    }
  }

  public abstract void inject(T target) throws InvocationTargetException, IllegalAccessException;

  public abstract void uninject(T target);

  protected void decouple(Channel channel) {
    if (channel.pipeline().get(this.getPacketEncoderName()) != null) {
      channel.pipeline().remove(this.getPacketEncoderName());
    }
    if (channel.pipeline().get(this.getPacketDecoderName()) != null) {
      channel.pipeline().remove(this.getPacketDecoderName());
    }
  }

  @Override
  protected void initChannel(Channel channel) {
    channel.pipeline().flush();
    if (channel.pipeline().get(this.getPacketEncoderName()) != null) {
      channel.pipeline().addAfter(this.getPacketEncoderName(), CHANNEL_OUT, this.channelOut());
    }
    if (channel.pipeline().get(this.getPacketDecoderName()) != null) {
      channel.pipeline().addAfter(this.getPacketDecoderName(), CHANNEL_IN, this.channelIn());
    }
  }

  protected void triggerIncoming(Object msg) {
    Class<?> type = msg.getClass();
    List<Consumer<Object>> callbacks = this.incomingPacketCallbacks.get(type);
    if (callbacks == null) {
      return;
    }
    callbacks.forEach(callback -> callback.accept(msg));
  }

  protected void triggerOutgoing(Object msg) {
    Class<?> type = msg.getClass();
    List<Consumer<Object>> callbacks = this.outgoingPacketCallbacks.get(type);
    if (callbacks == null) {
      return;
    }
    callbacks.forEach(callback -> callback.accept(msg));
  }

  public enum Direction {
    INCOMING, OUTGOING
  }
}
