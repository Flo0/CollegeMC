package net.collegemc.mc.core.widgetdisplays.craftle;

import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.displaywidgets.DisplayWidgetManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class CraftleManager {

  private final DisplayWidgetManager widgetManager;
  private Set<String> possibleWords;
  private static final int idSpace = 100;
  private static final int idLimit = 150;
  private static int currentId;
  private static final Color backgroundColor = Color.GRAY;

  public CraftleManager() {
    widgetManager = CollegeLibrary.getDisplayWidgetManager();
    currentId = idSpace;
    possibleWords = new HashSet<>();
    loadWords();
  }

  public void startCraftle(Location location, Player player) {
    if (currentId >= idLimit) {
      throw new RuntimeException("Craftle ran out of ids!");
    }
    CraftleDisplay craftleDisplay = new CraftleDisplay(currentId, location.toVector(), location.getDirection(), backgroundColor, possibleWords.stream().toList().get(ThreadLocalRandom.current().nextInt(possibleWords.size())));
    widgetManager.createWindow(craftleDisplay, location.getWorld());
    widgetManager.engage(player, currentId);
    currentId++;
  }

  public boolean validWord(String word) {
    return possibleWords.contains(word);
  }

  private void loadWords() {

    InputStream input = CollegeCore.getInstance().getResource("valid-craftle-words.txt");
    if (input == null) {
      throw new RuntimeException("Craftle.txt not found");
    }
    InputStreamReader reader = new InputStreamReader(input);
    BufferedReader bufferedReader = new BufferedReader(reader);
    possibleWords = bufferedReader.lines().collect(Collectors.toUnmodifiableSet());
  }


}
