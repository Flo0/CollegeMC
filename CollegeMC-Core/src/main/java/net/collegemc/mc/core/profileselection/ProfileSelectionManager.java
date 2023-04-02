package net.collegemc.mc.core.profileselection;

import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.selectionmenu.SelectionMenuManager;
import net.collegemc.mc.libs.tasks.MongoBackedMap;
import org.bukkit.entity.Player;

import java.io.Flushable;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ProfileSelectionManager implements Flushable {

  public static final String NAMESPACE = "Profile-Selection-Locations";

  private final MongoBackedMap<String, ProfileSelectionLocation> selectionLocations;

  public ProfileSelectionManager() {
    this.selectionLocations = new MongoBackedMap<>(new HashMap<>(), NAMESPACE, String.class, ProfileSelectionLocation.class);
    this.selectionLocations.loadDataFromRemote();
  }

  public String enterSelection(Player player) {
    SelectionMenuManager selectionMenuManager = CollegeLibrary.getSelectionMenuManager();
    Optional<ProfileSelectionLocation> optLocation = this.selectionLocations.values().stream().filter(loc -> !loc.isOccupied()).findAny();
    if (optLocation.isEmpty()) {
      return null;
    }
    ProfileSelectionMenu selectionMenu = new ProfileSelectionMenu(player, optLocation.get());
    selectionMenuManager.startSelection(player, selectionMenu);
    return optLocation.get().getName();
  }

  public CompletableFuture<Void> tpTo(Player player, String location) {
    return CollegeCore.getTeleportManager().teleport(player, this.selectionLocations.get(location).getPlayerLocation());
  }

  @Override
  public void flush() {
    this.selectionLocations.saveDataToRemote();
  }

  public boolean nameExists(String name) {
    return this.selectionLocations.containsKey(name);
  }

  public void addSelection(ProfileSelectionLocation selectionLocation) {
    this.selectionLocations.put(selectionLocation.getName(), selectionLocation);
  }

  public List<String> getLocationNames() {
    return List.copyOf(this.selectionLocations.keySet());
  }

  public void removeLocation(String name) {
    this.selectionLocations.remove(name);
  }

}
