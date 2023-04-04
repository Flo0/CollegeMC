package net.collegemc.common.mineskin.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Texture {
  private String value;
  private String signature;
  private String url;

  public Texture() {
  }
}
