package net.collegemc.common.network.data.college;

import lombok.Getter;
import lombok.Setter;
import net.collegemc.common.network.data.college.attributes.AttributeContainer;
import net.collegemc.common.network.data.college.courses.CourseContainer;
import net.collegemc.common.network.data.college.traits.TraitContainer;

import java.util.UUID;

@Getter
@Setter
public class CollegeProfile {

  public CollegeProfile(ProfileId profileId) {
    this.collegeProfileId = profileId;
    this.attributeContainer = new AttributeContainer();
    this.traitContainer = new TraitContainer();
    this.courseContainer = new CourseContainer();
  }

  public CollegeProfile() {
    this(null);
  }

  private ProfileId collegeProfileId;
  private UUID minecraftUserId;
  private String name = "NO_NAME";
  private AttributeContainer attributeContainer;
  private TraitContainer traitContainer;
  private CourseContainer courseContainer;
  private String skinName;

  @Override
  public int hashCode() {
    return this.collegeProfileId.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof CollegeProfile other && this.collegeProfileId.equals(other.collegeProfileId);
  }

}
