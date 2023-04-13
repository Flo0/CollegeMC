package net.collegemc.common.utils;

public class StringUtils {

  public static String progressBar(String progressPrefix, String emptyPrefix, int length, double current, double max) {
    return progressBar(progressPrefix, emptyPrefix, "|", length, current, max);
  }

  public static String progressBar(String progressPrefix, String emptyPrefix, int length, double progress) {
    return progressBar(progressPrefix, emptyPrefix, "|", length, progress);
  }

  public static String progressBar(String progressPrefix, String emptyPrefix, String delim, int length, double current, double max) {
    double progress = 1.0 / max * current;
    int progressLength = (int) (progress * length);
    int remainingLength = length - progressLength;
    return progressPrefix + delim.repeat(progressLength) + emptyPrefix + delim.repeat(remainingLength);
  }

  public static String progressBar(String progressPrefix, String emptyPrefix, String delim, int length, double progress) {
    int progressLength = (int) (progress * length);
    int remainingLength = length - progressLength;
    return progressPrefix + delim.repeat(progressLength) + emptyPrefix + delim.repeat(remainingLength);
  }

  public static String leftPad(String input, int amount) {
    int length = input.length();
    if (length >= amount) {
      return input;
    }
    return " ".repeat(amount - length) + input;
  }

  public static String rightPad(String input, int amount) {
    int length = input.length();
    if (length >= amount) {
      return input;
    }
    return input + " ".repeat(amount - length);
  }

}
