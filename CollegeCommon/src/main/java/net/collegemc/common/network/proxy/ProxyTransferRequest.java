package net.collegemc.common.network.proxy;

import lombok.Builder;
import lombok.Data;
import net.collegemc.common.bridge.RemoteEvent;

import java.util.UUID;

@Data
@Builder
public class ProxyTransferRequest implements RemoteEvent {

  public static final String CHANNEL = "_USER_TRANSFER_";

  private UUID userId;
  private String server;
  private boolean indicated;

}
