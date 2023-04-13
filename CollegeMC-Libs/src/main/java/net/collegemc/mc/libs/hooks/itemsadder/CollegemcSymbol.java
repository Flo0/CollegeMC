package net.collegemc.mc.libs.hooks.itemsadder;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum CollegemcSymbol {

  COIN("cmccoin");

  private final String name;

  public String get() {
    return ItemsAdderHook.getFontImage(name);
  }

  @Override
  public String toString() {
    return get();
  }
}
