package net.collegemc.mc.libs.actionbar;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.collegemc.common.utils.StringUtils;

public class ActionBarBoard {

  protected static final int MIN_SECTION_LENGTH = 20;

  protected ActionBarBoard() {
    this.sections = new ActionBarSection[3];
    this.sections[0] = new ActionBarSection();
    this.sections[1] = new ActionBarSection();
    this.sections[2] = new ActionBarSection();
  }

  private final ActionBarSection[] sections;
  @Getter
  private String currentDisplay = "";
  @Setter
  private String delimiter = " ";

  public void update() {
    final String left = this.sections[0].getMostSignificantLayer().getLineSupplier().get();
    String middle = this.sections[1].getMostSignificantLayer().getLineSupplier().get();
    final String right = this.sections[2].getMostSignificantLayer().getLineSupplier().get();

    final int pad = Math.max(1, (MIN_SECTION_LENGTH - middle.length()) / 2);
    if (left.isEmpty() && middle.isEmpty() && right.isEmpty()) {
      this.currentDisplay = "";
    } else {
      middle = Strings.repeat(" ", pad) + middle + Strings.repeat(" ", pad);
      this.currentDisplay = "§f" + StringUtils.leftPad(left, 20) + "§7 "
              + this.delimiter + " §f"
              + middle + "§7 "
              + this.delimiter + " §f"
              + StringUtils.rightPad(right, 20);
    }
  }

  public ActionBarSection getSection(final Section section) {
    return this.getSection(section.index);
  }

  public ActionBarSection getSection(final int index) {
    Preconditions.checkArgument(index < 3, "Index must be below 3.");
    return this.sections[index];
  }

  @AllArgsConstructor
  public enum Section {
    LEFT(0), MIDDLE(1), RIGHT(2);
    @Getter
    private final int index;
  }

}
