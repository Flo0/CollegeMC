package net.collegemc.mc.libs.resourcepack.assembly;


public class BoxedFontChar {

  protected char value = '⛔';

  public char getAsCharacter() {
    return this.value;
  }

  @Override
  public String toString() {
    return String.valueOf(this.value);
  }

}
