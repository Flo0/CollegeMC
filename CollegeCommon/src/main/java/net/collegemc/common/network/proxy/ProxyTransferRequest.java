package net.collegemc.common.network.proxy;

import net.collegemc.common.bridge.RemoteEvent;

import java.util.UUID;

public class ProxyTransferRequest implements RemoteEvent {

  public static final String CHANNEL = "_USER_TRANSFER_";

  private UUID userId;
  private String server;
  private boolean indicated;

}
