package net.collegemc.mc.libs.tablist.implementation;


import com.google.common.collect.Lists;
import net.collegemc.mc.libs.tablist.abstraction.TabLine;
import net.collegemc.mc.libs.tablist.abstraction.TabList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public abstract class AbstractTabList implements TabList {

  private final Set<ServerGamePacketListenerImpl> playerConnectionSet;
  protected final ArrayList<TabLine> tabs;
  private ClientboundTabListPacket headerFooterPacket;

  public AbstractTabList() {
    this.playerConnectionSet = new HashSet<>();
    this.tabs = Lists.newArrayList();
    this.headerFooterPacket = new ClientboundTabListPacket(Component.literal("EMPTY_HEADER"), Component.literal("EMPTY_FOOTER"));
  }

  @Override
  public void addViewer(final ServerGamePacketListenerImpl connection) {
    this.playerConnectionSet.add(connection);
  }

  @Override
  public void removeViewer(final ServerGamePacketListenerImpl connection) {
    this.playerConnectionSet.remove(connection);
  }

  @Override
  public Set<ServerGamePacketListenerImpl> getViewers() {
    return this.playerConnectionSet;
  }

  @Override
  public int getSize() {
    return this.tabs.size();
  }

  @Override
  public TabLine getLine(final int index) {
    return this.tabs.get(index);
  }

  @Override
  public void setHeader(final String header) {
    this.headerFooterPacket = new ClientboundTabListPacket(Component.literal(header), this.headerFooterPacket.footer);
  }

  @Override
  public String getHeader() {
    return this.headerFooterPacket.header.getString();
  }

  @Override
  public void setFooter(final String footer) {
    this.headerFooterPacket = new ClientboundTabListPacket(this.headerFooterPacket.header, Component.literal(footer));
  }

  @Override
  public String getFooter() {
    return this.headerFooterPacket.footer.getString();
  }

  @Override
  public void sendHeaderFooter(final ServerGamePacketListenerImpl connection) {
    connection.send(this.headerFooterPacket);
  }

  @Override
  public void addLine(final TabLine line) {
    this.tabs.add(line);
  }

  public abstract void init();

}