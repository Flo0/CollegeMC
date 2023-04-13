package net.collegemc.mc.core.util;

import net.collegemc.common.network.data.college.CollegeProfile;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.libs.gui.abstraction.GuiButton;
import net.collegemc.mc.libs.gui.baseimpl.DynamicGUI;
import net.collegemc.mc.libs.spigot.ItemBuilder;
import net.collegemc.mc.libs.spigot.UtilItem;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class OnlineProfileSelectionGUI extends DynamicGUI {

  private final Predicate<CollegeProfile> filter;
  private final Consumer<CollegeProfile> consumer;
  private final String text;

  public OnlineProfileSelectionGUI(Consumer<CollegeProfile> consumer, Predicate<CollegeProfile> filter, String text) {
    this.filter = filter;
    this.consumer = consumer;
    this.text = text;
  }

  public OnlineProfileSelectionGUI(Consumer<CollegeProfile> consumer, String text) {
    this(consumer, any -> true, text);
  }

  @Override
  protected void setupButtons() {
    Bukkit.getOnlinePlayers().stream()
            .map(ActiveCollegeUser::of)
            .map(ActiveCollegeUser::getCurrentCollegeProfile)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(this.filter)
            .map(this::createProfileSelectionButton)
            .forEach(this::addButton);

    this.setButton(49, this.createEmptySelectionButton());
  }

  private GuiButton createEmptySelectionButton() {
    return GuiButton.builder()
            .eventConsumer(event -> {
              UtilPlayer.playUIClick((Player) event.getWhoClicked());
              this.consumer.accept(null);
            }).iconCreator(() -> ItemBuilder.of(Material.RED_CONCRETE)
                    .name("§6No one")
                    .lore("")
                    .lore("§7Click to select no one")
                    .build())
            .build();
  }

  private GuiButton createProfileSelectionButton(CollegeProfile profile) {
    return GuiButton.builder()
            .eventConsumer(event -> {
              UtilPlayer.playUIClick((Player) event.getWhoClicked());
              this.consumer.accept(profile);
            }).iconCreator(() -> ItemBuilder.of(UtilItem.produceHead(profile.getSkin()))
                    .name("§6" + profile.getName())
                    .lore("")
                    .lore("§7Click to select this player")
                    .build())
            .build();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 6 * 9, Component.text(this.text));
  }
}
