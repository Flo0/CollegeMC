package net.collegemc.common.network.data.network;

import lombok.Data;
import net.collegemc.common.network.data.college.ProfileId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class NetworkUserData {

  public NetworkUserData(UUID minecraftUid) {
    this.minecraftUid = minecraftUid;
    this.collegeProfiles = new ArrayList<>();
  }

  public NetworkUserData() {
    this(null);
  }

  private final List<ProfileId> collegeProfiles;
  private String lastSeenMinecraftName;
  private UUID minecraftUid;
  private Long registeredDiscordId;
  private NetworkRank networkRank = NetworkRank.USER;
  private long lastLoginTimestamp = 0L;
  private ProfileId activeCollegeProfileId = null;

}
