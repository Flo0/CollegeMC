package net.collegemc.common.utils;

public class StringUtils {

  public static String leftPad(String input, int amount) {
    int length = input.length();
    if(length >= amount) {
      return input;
    }
    return " ".repeat(amount - length) + input;
  }

  public static String rightPad(String input, int amount) {
    int length = input.length();
    if(length >= amount) {
      return input;
    }
    return input + " ".repeat(amount - length);
  }

}
