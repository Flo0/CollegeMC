package net.collegemc.mc.libs.actionbar;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Supplier;

public class ActionLine implements Comparable<ActionLine> {

  public static final int MIN_PRIORITY = 1000000;
  public static final int LOW_PRIORITY = 100000;
  public static final int MID_PRIORITY = 10000;
  public static final int HIGH_PRIORITY = 1000;
  public static final int VERY_HIGH_PRIORITY = 100;
  public static final int MAX_PRIORITY = 10;
  private static final Supplier<String> EMPTY_SUPPLIER = () -> Strings.repeat(" ", ActionBarBoard.MIN_SECTION_LENGTH);
  private static final String EMPTY_IDENTITY = "_EMPTY_";

  public static ActionLine defaultLine() {
    return new ActionLine(EMPTY_IDENTITY, MIN_PRIORITY, EMPTY_SUPPLIER);
  }

  public ActionLine(String id, int priority, String simpleLine) {
    this(id, priority, () -> simpleLine);
  }

  public ActionLine(String id, int priority, Supplier<String> lineSupplier) {
    this.priority = priority;
    this.lineSupplier = lineSupplier;
    this.identity = id;
  }

  @Getter
  private final String identity;
  @Getter
  @Setter
  private int priority;
  @Getter
  @Setter
  private Supplier<String> lineSupplier;

  @Override
  public int compareTo(ActionLine other) {
    return this.priority - other.priority;
  }

  @Override
  public int hashCode() {
    return this.identity.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ActionLine actionLine)) {
      return false;
    }
    return actionLine.identity.equals(this.identity);
  }
}