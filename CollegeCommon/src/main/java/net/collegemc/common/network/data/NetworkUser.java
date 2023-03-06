package net.collegemc.common.network.data;

import lombok.Data;

import java.util.UUID;

@Data
public class NetworkUser {

  public NetworkUser(UUID minecraftUid) {
    this.minecraftUid = minecraftUid;
  }

  public NetworkUser() {
    this(null);
  }

  private String lastSeenMinecraftName;
  private UUID minecraftUid;
  private Long registeredDiscordId;
  private NetworkRank networkRank = NetworkRank.USER;
  private long lastLoginTime = 0L;

}
