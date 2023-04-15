package net.collegemc.mc.libs.displaywidgets;

import lombok.Getter;
import lombok.Setter;
import net.collegemc.mc.libs.displaywidgets.events.ClickEvent;
import net.collegemc.mc.libs.displaywidgets.events.WidgetEvent;
import net.collegemc.mc.libs.tasks.TaskManager;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class WidgetButton extends WidgetText {

  private final ButtonType type;
  @Setter
  private ButtonDisplayProperties defaultProperties;
  @Setter
  private ButtonDisplayProperties pressedProperties;
  @Getter
  private boolean pressed;
  @Getter
  @Setter
  private int buttonAutoReleaseTicks = 10;
  private BukkitTask autoReleaseTask = null;

  public WidgetButton(Vec2f position, int width, int height, ButtonType type, ButtonDisplayProperties defaultProperties, ButtonDisplayProperties pressedProperties) {
    super(position, width, height, defaultProperties.backgroundColor, defaultProperties.opacity);
    this.type = type;
    this.pressed = false;
    this.defaultProperties = defaultProperties;
    this.pressedProperties = pressedProperties;
    this.setVerticalAlignment(VerticalAlignment.CENTER);
  }

  @Override
  public void update() {
    ButtonDisplayProperties currentProperties = pressed ? pressedProperties : defaultProperties;
    this.setBackgroundColor(currentProperties.backgroundColor);
    this.setOpacity(currentProperties.opacity);
    super.update();
  }

  public void interact() {
    this.interact(null);
  }

  private void interact(@Nullable Player actor) {
    if (type == ButtonType.TOGGLEABLE) {
      setButtonPressState(!pressed, actor);
    } else if (type == ButtonType.CLICKABLE) {
      setButtonPressState(true, actor);
      if (this.autoReleaseTask != null) {
        this.autoReleaseTask.cancel();
      }
      this.autoReleaseTask = TaskManager.runTaskLater(() -> {
        this.autoReleaseTask = null;
        setButtonPressState(false, actor);
      }, this.buttonAutoReleaseTicks);
    }
  }

  @Override
  public void onClick(ClickEvent event) {
    interact(event.getActor());
    super.onClick(event);
  }

  @SuppressWarnings("unchecked")
  private void setButtonPressState(boolean state, @Nullable Player actor) {
    ButtonStateChangedEvent event = new ButtonStateChangedEvent(actor, state);
    this.eventHandlers.getOrDefault(ButtonStateChangedEvent.class, List.of()).forEach(handler -> ((Consumer<ButtonStateChangedEvent>) handler).accept(event));
    this.pressed = state;
    update();
  }

  public enum ButtonType {
    TOGGLEABLE,
    CLICKABLE
  }

  public record ButtonDisplayProperties(Color backgroundColor, double opacity) {
  }

  public static class ButtonStateChangedEvent extends WidgetEvent {
    @Getter
    private final boolean pressedState;

    public ButtonStateChangedEvent(Player actor, boolean targetState) {
      super(actor, new Vec2f(0, 0));
      this.pressedState = targetState;
    }
  }
}
