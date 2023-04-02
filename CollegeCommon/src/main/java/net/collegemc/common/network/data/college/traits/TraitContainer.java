package net.collegemc.common.network.data.college.traits;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TraitContainer {

  private final Set<CollegeTrait> traitSet = new HashSet<>();

  public boolean hasTrait(CollegeTrait trait) {
    return this.traitSet.contains(trait);
  }

  public void addTrait(CollegeTrait trait) {
    this.traitSet.add(trait);
  }

  public void removeTrait(CollegeTrait trait) {
    this.traitSet.remove(trait);
  }

  public List<CollegeTrait> getTraits() {
    return List.copyOf(this.traitSet);
  }

}
