package net.collegemc.mc.core.profileselection;

import net.collegemc.common.mineskin.data.Skin;
import net.collegemc.common.network.data.college.CollegeProfile;
import net.collegemc.mc.core.active.ActiveCollegeUser;
import net.collegemc.mc.libs.gui.abstraction.GuiButton;
import net.collegemc.mc.libs.gui.baseimpl.DynamicGUI;
import net.collegemc.mc.libs.spigot.ItemBuilder;
import net.collegemc.mc.libs.spigot.UtilItem;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class ProfileSelectionGUI extends DynamicGUI {

  private final List<CollegeProfile> profiles;
  private final Consumer<CollegeProfile> selectionConsumer;

  public ProfileSelectionGUI(Player player, Consumer<CollegeProfile> selectionConsumer) {
    this.selectionConsumer = selectionConsumer;
    ActiveCollegeUser collegeUser = ActiveCollegeUser.of(player);
    this.profiles = collegeUser.getProfileList();
  }

  @Override
  protected void setupButtons() {
    int index = 10;
    for (CollegeProfile profile : profiles) {
      GuiButton button = createProfileButton(profile);
      setButton(index, button);
      if (index == 16) {
        index = 28;
      } else {
        index += 2;
      }
    }
  }

  private GuiButton createProfileButton(CollegeProfile profile) {
    return GuiButton.builder()
            .iconCreator(() -> createProfileItem(profile))
            .asyncCreated(false)
            .eventConsumer(event -> {
              event.getWhoClicked().closeInventory();
              UtilPlayer.playUIClick((Player) event.getWhoClicked());
              selectionConsumer.accept(profile);
            }).build();
  }

  private ItemStack createProfileItem(CollegeProfile profile) {
    Skin skin = profile.getSkin();
    ItemStack headItem = UtilItem.produceHead(skin);
    return ItemBuilder.of(headItem)
            .name("§6" + profile.getName())
            .lore("")
            .lore("§7Click to select this profile")
            .build();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 9 * 4, Component.text("Select your profile"));
  }
}
