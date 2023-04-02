package net.collegemc.mc.libs.spigot;

import net.collegemc.mc.libs.CollegeLibrary;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NameGenerator {

  private static class Names {

    private List<String> firstNames;
    private List<String> lastNames;

    private Names(List<String> firstNames, List<String> lastNames) {
      this.firstNames = firstNames;
      this.lastNames = lastNames;
    }
  }

  private static final double UNDERSCORE_CHANCE = 0.05;
  private static final double NUMBER_CHANCE = 0.75;

  private final Names names;

  public NameGenerator(File file) {
    try {
      this.names = CollegeLibrary.getGsonSerializer().fromJson(Files.readString(file.toPath()), Names.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String generate() {
    ThreadLocalRandom random = ThreadLocalRandom.current();
    String first = this.names.firstNames.get(random.nextInt(this.names.firstNames.size()));
    String last = this.names.lastNames.get(random.nextInt(this.names.lastNames.size()));
    StringBuilder builder = new StringBuilder();
    if (random.nextDouble() < UNDERSCORE_CHANCE) {
      builder.append("_");
    }
    builder.append(first);
    builder.append(last);
    if (random.nextDouble() < NUMBER_CHANCE) {
      builder.append(random.nextInt(0, 100));
    }
    return builder.toString();
  }


}
