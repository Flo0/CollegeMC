package net.collegemc.mc.libs.displaywidgets;

import lombok.Getter;
import org.joml.Vector3f;

public class Vec2f {

  @Getter
  private Vector3f vector;
  public float x;
  public float y;

  public Vec2f(Vector3f vector) {
    this.vector = vector;
    syncXY();
  }

  public Vec2f(float x, float y) {
    this.vector = new Vector3f(x, y, 0.0f);
    syncXY();
  }

  public void setVector(Vector3f vector) {
    this.vector = vector;
    syncXY();
  }

  public Vec2f add(Vec2f other) {
    this.vector.add(other.toVector3f());
    syncXY();
    return this;
  }

  public Vec2f negated() {
    this.vector.negate();
    syncXY();
    return this;
  }

  public Vector3f toVector3f() {
    return vector;
  }

  public Vec2f clone() {
    return new Vec2f(this.x, this.y);
  }

  private void syncXY() {
    this.x = vector.x;
    this.y = vector.y;
  }

  @Override
  public String toString() {
    return "x/y: " + x + "/" + y;
  }
}
