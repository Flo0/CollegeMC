package net.collegemc.mc.core.friends;

import net.collegemc.common.network.data.college.ProfileId;

import java.util.HashSet;
import java.util.Set;

public class FriendRequests {

  private final Set<ProfileId> sentTo;
  private final Set<ProfileId> receivedFrom;

  public FriendRequests() {
    this.sentTo = new HashSet<>();
    this.receivedFrom = new HashSet<>();
  }

  public boolean hasSentTo(ProfileId profileId) {
    return this.sentTo.contains(profileId);
  }

  public boolean hasReceivedFrom(ProfileId profileId) {
    return this.receivedFrom.contains(profileId);
  }

  public void addSentTo(ProfileId profileId) {
    this.sentTo.add(profileId);
  }

  public void addReceivedFrom(ProfileId profileId) {
    this.receivedFrom.add(profileId);
  }

  public void removeSentTo(ProfileId profileId) {
    this.sentTo.remove(profileId);
  }

  public void removeReceivedFrom(ProfileId profileId) {
    this.receivedFrom.remove(profileId);
  }

}
