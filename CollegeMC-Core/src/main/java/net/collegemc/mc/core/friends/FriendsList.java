package net.collegemc.mc.core.friends;

import lombok.Getter;
import net.collegemc.common.network.data.college.ProfileId;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FriendsList implements Iterable<ProfileId> {

  @Getter
  private final ProfileId ownerProfileId;
  private final Set<ProfileId> friends;

  public FriendsList(ProfileId ownerProfileId) {
    this.ownerProfileId = ownerProfileId;
    this.friends = new LinkedHashSet<>();
  }

  protected FriendsList() {
    this(null);
  }

  public int size() {
    return this.friends.size();
  }

  public void addFriend(ProfileId friendId) {
    this.friends.add(friendId);
  }

  public void removeFriend(ProfileId friendId) {
    this.friends.remove(friendId);
  }

  public boolean isFriends(ProfileId profileId) {
    return this.friends.contains(profileId);
  }

  public List<ProfileId> getFriends() {
    return List.copyOf(this.friends);
  }

  @NotNull
  @Override
  public Iterator<ProfileId> iterator() {
    return List.copyOf(this.friends).iterator();
  }
}
