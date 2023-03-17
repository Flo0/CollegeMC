package net.collegemc.mc.libs.regions.impl;

import lombok.Getter;

@Getter
public class Vec2D {

  private final double x;
  private final double z;

  public Vec2D(double x, double z) {
    this.x = x;
    this.z = z;
  }
}
