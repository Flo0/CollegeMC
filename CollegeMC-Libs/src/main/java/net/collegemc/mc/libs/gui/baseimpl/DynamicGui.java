package net.collegemc.mc.libs.gui.baseimpl;


import net.collegemc.mc.libs.gui.abstraction.GuiHandler;

public abstract class DynamicGui extends GuiHandler {

  @Override
  public void decorate() {
    this.setupButtons();
    super.decorate();
  }

  protected abstract void setupButtons();

}
