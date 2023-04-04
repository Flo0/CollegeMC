package net.collegemc.common.mineskin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Visibility {
  PUBLIC(0),
  PRIVATE(1);

  private final int code;
}
