package net.collegemc.common.mineskin.data;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SkinData {
  private UUID uuid;
  private Texture texture;

  public SkinData() {
  }
}
