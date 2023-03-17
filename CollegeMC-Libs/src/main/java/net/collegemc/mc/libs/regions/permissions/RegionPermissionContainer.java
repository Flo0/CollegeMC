package net.collegemc.mc.libs.regions.permissions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RegionPermissionContainer {

  private final Map<UUID, Set<RegionPermission>> permissions = new HashMap<>();

  public boolean hasPermission(UUID userId, RegionPermission permission) {
    Set<RegionPermission> permissionSet = this.permissions.get(userId);
    return permissionSet != null && permissionSet.contains(permission);
  }

  public void addPermission(UUID userId, RegionPermission permission) {
    this.permissions.computeIfAbsent(userId, key -> EnumSet.noneOf(RegionPermission.class)).add(permission);
  }

  public void removePermission(UUID userId, RegionPermission permission) {
    Set<RegionPermission> permissionSet = this.permissions.get(userId);
    if (permissionSet == null) {
      return;
    }
    permissionSet.remove(permission);
    if (permissionSet.isEmpty()) {
      this.permissions.remove(userId);
    }
  }

  public List<UUID> getAllowedUsersFor(RegionPermission permission) {
    List<UUID> ids = new ArrayList<>();
    for (UUID userId : List.copyOf(this.permissions.keySet())) {
      if (this.hasPermission(userId, permission)) {
        ids.add(userId);
      }
    }
    return ids;
  }

}
