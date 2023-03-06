package net.collegemc.mc.libs.gui.baseimpl;

import com.google.common.base.Preconditions;

import java.util.HashSet;
import java.util.Set;

public abstract class StaticGui extends DynamicGui {

  private static final Set<Class<? extends StaticGui>> instanceSafeguard = new HashSet<>();

  public StaticGui() {
    Preconditions.checkState(!instanceSafeguard.contains(this.getClass()), "Dont instantiate static GUIs multiple times.");
    instanceSafeguard.add(this.getClass());
  }

  @Override
  public boolean unregisterOnClose() {
    return false;
  }
}
