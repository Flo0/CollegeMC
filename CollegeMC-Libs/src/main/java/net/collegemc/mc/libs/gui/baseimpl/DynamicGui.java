package net.collegemc.mc.libs.gui.baseimpl;


public abstract non-sealed class DynamicGui extends GuiHandler {

  @Override
  public void decorate() {
    if (!this.isDecorated()) {
      this.setupButtons();
    }
    super.decorate();
  }

  protected abstract void setupButtons();

}
