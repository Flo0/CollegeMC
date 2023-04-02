package net.collegemc.common.network.data.college;

import lombok.Getter;

import java.util.UUID;

/**
 * Wrapper class of UUID for clarity reasons.
 * Prevents mixing up minecraft account UUIDs.
 */
public class ProfileId {

  public static ProfileId random() {
    return new ProfileId(UUID.randomUUID());
  }

  @Getter
  private final UUID uid;

  public ProfileId(UUID uid) {
    this.uid = uid;
  }

  @Override
  public int hashCode() {
    return this.uid.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof UUID otherId) {
      return this.uid.equals(otherId);
    } else if (obj instanceof ProfileId otherProfileId) {
      return this.uid.equals(otherProfileId.uid);
    } else {
      return false;
    }
  }
}
