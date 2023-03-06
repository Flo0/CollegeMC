package net.collegemc.common.network.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum NetworkRank {

  USER(),
  BUILDER(USER),
  DEVELOPER(USER),
  ADMIN(DEVELOPER, BUILDER);

  private final Set<NetworkRank> inheritedRanks;

  NetworkRank(NetworkRank... inherited) {
    this.inheritedRanks = new HashSet<>();

    List<NetworkRank> rankList = new ArrayList<>();
    for (NetworkRank rank : inherited) {
      rank.addInherits(rankList);
    }

    inheritedRanks.addAll(rankList);
    this.inheritedRanks.add(this);
  }

  private void addInherits(List<NetworkRank> rankList) {
    if (rankList.contains(this)) {
      return;
    }
    rankList.add(this);
    rankList.addAll(inheritedRanks);
    inheritedRanks.forEach(rank -> rank.addInherits(rankList));
  }

  public boolean hasClearanceOf(NetworkRank rank) {
    return this.inheritedRanks.contains(rank);
  }

}
