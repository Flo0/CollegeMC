package net.collegemc.mc.libs.resourcepack.assembly;

public record FontMeta(int horizontalShift, int verticalShift, int height, int ascent, String type) {

  public static FontMeta of(final int height, final int ascent, final String type) {
    return new FontMeta(0, 0, height, ascent, type);
  }

  public static FontMeta of(final int height, final int ascent) {
    return new FontMeta(0, 0, height, ascent, "bitmap");
  }

  public static FontMeta common() {
    return new FontMeta(0, 0, 9, 8, "bitmap");
  }

  public static FontMeta iconFont() {
    return new FontMeta(20, 0, 38, 12, "bitmap");
  }

  public static FontMeta tabFont() {
    return FontMeta.of(52, 28, "bitmap");
  }

  public static FontMeta gradientFont() {
    return new FontMeta(0, 0, 44, 8, "bitmap");
  }

  public static FontMeta gradientFontAs() {
    return new FontMeta(0, 0, 44, 26, "bitmap");
  }

}
