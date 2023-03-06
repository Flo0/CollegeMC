package net.collegemc.common.network.data.college;

import lombok.Data;

import java.util.UUID;

@Data
public class CollegeProfile {

  public CollegeProfile(UUID profileId) {
    this.collegeProfileId = profileId;
  }

  public CollegeProfile() {
    this(null);
  }

  private UUID collegeProfileId;
  private String firstName = "NO_NAME";
  private String lastName = "NO_NAME";

  public String getFullName() {
    return this.firstName + " " + this.lastName;
  }

}
