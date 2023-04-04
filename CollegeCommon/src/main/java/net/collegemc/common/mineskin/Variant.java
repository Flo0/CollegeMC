package net.collegemc.common.mineskin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Variant {
  AUTO(""),
  CLASSIC("classic"),
  SLIM("slim");

  private final String name;
}
