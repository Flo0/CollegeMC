package net.collegemc.mc.libs.gui.baseimpl;

import net.collegemc.mc.libs.gui.abstraction.GuiButton;

import java.util.List;

public abstract class PaginatedDynamicGUI extends DynamicGUI {

  private final int elementsPerPage;
  protected List<GuiButton> contentList;
  private int pageIndex = 0;

  protected PaginatedDynamicGUI(int elementsPerPage) {
    this.elementsPerPage = elementsPerPage;
  }

  @Override
  protected void setupButtons() {
    int startIndex = pageIndex * elementsPerPage;
    int endIndex = Math.min((pageIndex + 1) * elementsPerPage, contentList.size());
    List<GuiButton> currentPageButtons = contentList.subList(startIndex, endIndex);
    for (int i = 0; i < currentPageButtons.size(); i++) {
      GuiButton button = currentPageButtons.get(i);
      setButton(i, button);
    }
  }

  protected boolean hasNextPage() {
    int lastPageIndex = (int) Math.ceil(contentList.size() / (double) elementsPerPage) - 1;
    return pageIndex < lastPageIndex;
  }

  protected boolean hasPreviousPage() {
    return pageIndex > 0;
  }

  protected boolean nextPage() {
    int lastPageIndex = (int) Math.ceil(contentList.size() / (double) elementsPerPage) - 1;
    if (pageIndex < lastPageIndex) {
      pageIndex++;
      this.decorate();
      return true;
    }
    return false;
  }

  protected boolean previousPage() {
    if (pageIndex > 0) {
      pageIndex--;
      this.decorate();
      return true;
    }
    return false;
  }

  protected abstract void setupContentList();

}
