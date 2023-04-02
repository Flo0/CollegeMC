package net.collegemc.mc.core.friends;

import net.collegemc.common.network.data.college.ProfileId;
import net.collegemc.mc.libs.gui.abstraction.GuiButton;
import net.collegemc.mc.libs.gui.baseimpl.DynamicGUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class FriendsMainGUI extends DynamicGUI {

  public FriendsMainGUI(Player player) {

  }

  @Override
  protected void setupButtons() {

  }

  private GuiButton createFriendButton(ProfileId friendId) {
    return GuiButton.builder().build();
  }

  private GuiButton createAddButton() {
    return GuiButton.builder().build();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 6 * 9, Component.text("Friends"));
  }
}
