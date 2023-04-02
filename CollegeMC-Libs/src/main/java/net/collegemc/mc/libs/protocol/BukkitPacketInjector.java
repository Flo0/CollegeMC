package net.collegemc.mc.libs.protocol;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BukkitPacketInjector extends AbstractPacketInjector<Player> {

  public static final String HANDLER_NAME = "packet_handler";

  private final ChannelHandler out = new OutgoingHandler();
  private final ChannelHandler in = new IncomingHandler();

  @Override
  protected String getPacketDecoderName() {
    return HANDLER_NAME;
  }

  @Override
  protected String getPacketEncoderName() {
    return HANDLER_NAME;
  }

  @Override
  protected ChannelHandler channelOut() {
    return this.out;
  }

  @Override
  protected ChannelHandler channelIn() {
    return this.in;
  }

  @Override
  public void inject(Player target) {
    this.initChannel(((CraftPlayer) target).getHandle().connection.connection.channel);
  }

  @Override
  public void uninject(Player target) {
    this.decouple(((CraftPlayer) target).getHandle().connection.connection.channel);
  }

  @Sharable
  private class IncomingHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      BukkitPacketInjector.this.triggerIncoming(msg);
      super.channelRead(ctx, msg);
    }
  }

  @Sharable
  private class OutgoingHandler extends ChannelDuplexHandler {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      BukkitPacketInjector.this.triggerOutgoing(msg);
      super.write(ctx, msg, promise);
    }
  }
}
