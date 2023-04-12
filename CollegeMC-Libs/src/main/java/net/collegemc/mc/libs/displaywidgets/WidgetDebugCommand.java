package net.collegemc.mc.libs.displaywidgets;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import lombok.Getter;
import net.collegemc.mc.libs.CollegeLibrary;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.kyori.adventure.text.Component;
import net.minecraft.world.phys.Vec2;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.util.Vector;

@CommandAlias("widgetdebug")
public class WidgetDebugCommand extends BaseCommand {

  @Getter
  private int currentEntityNumber = 0;
  private Player player;
  private Location mobLocation;

  @Default
  @CommandCompletion("x y z mobX mobY mobZ")
  public void onDefault(Player sender, int x, int y, int z, int mobx, int moby, int mobz) {
    this.player = sender;
    this.mobLocation = new Location(sender.getWorld(), mobx, moby, mobz);
    DisplayWidgetManager widgetManager = CollegeLibrary.getDisplayWidgetManager();

    //FRAME
    WidgetFrame frame = new WidgetFrame(0, new Vector(x, y, z), new Vector(0, 0, 1), 16, 12, Color.fromARGB(0, 97, 100, 110));
    WidgetText frameText = new WidgetText(6, Component.text(" ꀃ"), new Vec2(7, 6), 1, 1, Color.fromARGB(0, 0, 0, 255), 1.0);
    frameText.setVerticalAlignment(WidgetText.VerticalAlignment.CENTER);

    //TITLE
    WidgetText title = new WidgetText(1, Component.text("Select your Mob!"), new Vec2(5, 1), 6, 2, Color.fromARGB(0, 0, 0, 0), 1.0);
    title.setVerticalAlignment(WidgetText.VerticalAlignment.CENTER);

    //SELECTION
    WidgetText selection = new WidgetText(3, Component.text("Selection: " + MobSelection.values()[currentEntityNumber].name()), new Vec2(5, 5), 6, 2, Color.fromARGB(0, 0, 0, 0), 1.0);
    selection.setVerticalAlignment(WidgetText.VerticalAlignment.CENTER);

    //SELECTION BUTTONS
    WidgetButton.ButtonDisplayProperties invisible = new WidgetButton.ButtonDisplayProperties(Color.fromARGB(0, 255, 0, 0), 1.0);
    WidgetButton rightButton = new WidgetButton(5, new Vec2(13, 5), 2, 2, WidgetButton.ButtonType.CLICKABLE, invisible, invisible);
    WidgetButton leftButton = new WidgetButton(7, new Vec2(1, 5), 2, 2, WidgetButton.ButtonType.CLICKABLE, invisible, invisible);
    WidgetButton spawnButton = new WidgetButton(9, new Vec2(4, 8), 8, 2, WidgetButton.ButtonType.CLICKABLE, invisible, invisible);

    rightButton.setVerticalAlignment(WidgetText.VerticalAlignment.CENTER);
    leftButton.setVerticalAlignment(WidgetText.VerticalAlignment.CENTER);
    spawnButton.setVerticalAlignment(WidgetText.VerticalAlignment.CENTER);

    rightButton.setText(Component.text("ꀀ"));
    leftButton.setText(Component.text("ꀁ"));
    spawnButton.setText(Component.text("ꀂ"));

    rightButton.addEventHandler(WidgetButton.ButtonStateChangedEvent.class, buttonStateChangedEvent -> {
      if (buttonStateChangedEvent.isPressedState()) {
        CollegeLibrary.getInstance().getLogger().warning("RIGHT");
        currentEntityNumber = (currentEntityNumber + 1) % 4;
        selection.setText(Component.text("Selection: " + MobSelection.values()[currentEntityNumber].name()));
        selection.update();
        rightButton.setText(Component.text("ꀄ"));
        rightButton.update();
        UtilPlayer.playUIClick(sender);
      } else {
        rightButton.setText(Component.text("ꀀ"));
        rightButton.update();
      }
    });

    leftButton.addEventHandler(WidgetButton.ButtonStateChangedEvent.class, buttonStateChangedEvent -> {
      if (buttonStateChangedEvent.isPressedState()) {
        currentEntityNumber = (currentEntityNumber - 1);
        if (currentEntityNumber < 0) {
          currentEntityNumber = 3;
        }
        selection.setText(Component.text("Selection: " + MobSelection.values()[currentEntityNumber].name()));
        selection.update();
        leftButton.setText(Component.text("ꀅ"));
        leftButton.update();
        UtilPlayer.playUIClick(sender);
      } else {
        leftButton.setText(Component.text("ꀁ"));
        leftButton.update();
      }
    });

    spawnButton.addEventHandler(WidgetButton.ButtonStateChangedEvent.class, buttonStateChangedEvent -> {
      if (buttonStateChangedEvent.isPressedState()) {
        spawnEntity();
        spawnButton.setText(Component.text("ꀆ"));
        spawnButton.update();
        UtilPlayer.playUIClick(sender);
      } else {
        spawnButton.setText(Component.text("ꀂ"));
        spawnButton.update();
      }
    });


    frame.addChild(title);
    frame.addChild(selection);
    frame.addChild(leftButton);
    frame.addChild(rightButton);
    frame.addChild(spawnButton);
    // frame.addChild(frameText);
    widgetManager.createWindow(frame, sender.getWorld());
    widgetManager.engage(sender, 0);
  }

  private void spawnEntity() {
    player.getWorld().spawn(mobLocation, MobSelection.values()[currentEntityNumber].getEntityClass());
  }

  private enum MobSelection {
    CHICKEN(Chicken.class),
    PIG(Pig.class),
    COW(Cow.class),
    SHEEP(Sheep.class);

    MobSelection(Class<? extends Entity> entityClass) {
      this.entityClass = entityClass;
    }

    @Getter
    private final Class<? extends Entity> entityClass;
  }

}
