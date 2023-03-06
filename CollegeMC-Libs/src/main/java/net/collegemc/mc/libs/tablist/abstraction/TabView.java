package net.collegemc.mc.libs.tablist.abstraction;


import lombok.Getter;
import net.collegemc.mc.libs.tablist.implementation.AbstractTabList;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;


public class TabView {

  public TabView(final Player player) {
    this.player = player;
    this.connection = ((CraftPlayer) player).getHandle().connection;
  }

  @Getter
  private final Player player;
  @Getter
  private final ServerGamePacketListenerImpl connection;
  @Getter
  private AbstractTabList tablist;

  public void setTablist(final AbstractTabList newTabList) {
    if (this.tablist != null) {
      this.tablist.removeViewer(this.connection);
    }
    this.tablist = newTabList;
    newTabList.addViewer(this.connection);
  }

  public void setAndUpdate(final AbstractTabList newTabList) {
    if (this.tablist != null) {
      this.tablist.hideFrom(this.connection);
    }
    this.setTablist(newTabList);
    newTabList.init();
    this.tablist.showTo(this.connection);
  }

  public void update() {
    this.tablist.showTo(this.connection);
  }

}