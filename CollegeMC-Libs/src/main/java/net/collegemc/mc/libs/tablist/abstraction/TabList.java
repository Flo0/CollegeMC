package net.collegemc.mc.libs.tablist.abstraction;


import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.Set;

public interface TabList {

  default void updateDisplay(final int index, final String display) {
    final TabLine tabline = this.getLine(index);
    tabline.setDisplay(display);
    for (final ServerGamePacketListenerImpl connection : this.getViewers()) {
      tabline.sendDisplayUpdate(connection);
    }
  }

  default void updateTexture(final int index, final String texture, final String signature) {
    final TabLine tabline = this.getLine(index);
    tabline.setTexture(texture, signature);
    for (final ServerGamePacketListenerImpl connection : this.getViewers()) {
      tabline.sendProfileUpdate(connection);
    }
  }

  void addViewer(ServerGamePacketListenerImpl connection);

  void removeViewer(ServerGamePacketListenerImpl connection);

  Set<ServerGamePacketListenerImpl> getViewers();

  int getSize();

  TabLine getLine(int index);

  void setHeader(String header);

  String getHeader();

  void setFooter(String footer);

  String getFooter();

  void sendHeaderFooter(ServerGamePacketListenerImpl connection);

  void addLine(TabLine line);

  default void broadcastHeaderFooter() {
    for (final ServerGamePacketListenerImpl connection : this.getViewers()) {
      this.sendHeaderFooter(connection);
    }
  }

  default void updateAndSendHeaderFooter(final String header, final String footer) {
    this.setHeader(header);
    this.setFooter(footer);
    this.broadcastHeaderFooter();
  }

  default void showTo(final ServerGamePacketListenerImpl connection) {
    for (int index = 0; index < this.getSize(); index++) {
      this.getLine(index).send(connection);
    }
    this.sendHeaderFooter(connection);
  }

  default void hideFrom(final ServerGamePacketListenerImpl connection) {
    for (int index = 0; index < this.getSize(); index++) {
      this.getLine(index).sendHide(connection);
    }
  }

}