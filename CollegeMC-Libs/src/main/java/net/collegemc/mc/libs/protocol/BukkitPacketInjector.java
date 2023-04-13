package net.collegemc.mc.libs.protocol;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BukkitPacketInjector extends AbstractPacketInjector<Player> {

  public static final String HANDLER_NAME = "packet_handler";
  private static final String DUPLEX_NAME = "packet_proxy";

  @Override
  protected String getPacketHandlerName() {
    return HANDLER_NAME;
  }

  @Override
  public void inject(Player target) {
    this.initChannel(target, ((CraftPlayer) target).getHandle().connection.connection.channel);
  }

  @Override
  public void uninject(Player target) {
    this.decouple(((CraftPlayer) target).getHandle().connection.connection.channel);
  }

  protected void initChannel(Player player, Channel channel) {
    channel.pipeline().flush();
    if (channel.pipeline().get(this.getPacketHandlerName()) != null) {
      channel.pipeline().addBefore(this.getPacketHandlerName(), DUPLEX_NAME, new PlayerConnectionDuplexProxy(player));
    }
  }

  protected void decouple(Channel channel) {
    if (channel.pipeline().get(DUPLEX_NAME) != null) {
      channel.pipeline().remove(DUPLEX_NAME);
    }
  }

  @ChannelHandler.Sharable
  private class PlayerConnectionDuplexProxy extends ChannelDuplexHandler {

    private final Player player;

    private PlayerConnectionDuplexProxy(Player player) {
      this.player = player;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      BukkitPacketInjector.this.triggerIncoming(player, msg);
      super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      BukkitPacketInjector.this.triggerOutgoing(player, msg);
      super.write(ctx, msg, promise);
    }
  }
}
