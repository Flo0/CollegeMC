package net.collegemc.mc.core.transport.warp;

import net.collegemc.mc.core.CollegeCore;
import net.collegemc.mc.libs.gui.abstraction.GuiButton;
import net.collegemc.mc.libs.gui.baseimpl.DynamicGUI;
import net.collegemc.mc.libs.messaging.Msg;
import net.collegemc.mc.libs.spigot.ItemBuilder;
import net.collegemc.mc.libs.spigot.UtilPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WarpGUI extends DynamicGUI {
  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 3 * 9, Component.text("Warps"));
  }

  @Override
  protected void setupButtons() {
    CollegeCore.getWarpManager().forEach(warp -> this.addButton(this.createWarpButton(warp)));
  }

  private GuiButton createWarpButton(Warp warp) {
    return GuiButton.builder()
            .iconCreator(() -> ItemBuilder.of(warp.getIcon())
                    .lore("", "§7[Click to TP]", "§7[Click with item to change]")
                    .name("§e" + warp.getName())
                    .build())
            .eventConsumer(event -> {
              Player player = (Player) event.getWhoClicked();
              ItemStack cursor = event.getCursor();
              if (cursor == null || cursor.getType().isAir()) {
                CollegeCore.getTeleportManager().teleport(player, warp.getLocation()).thenRun(() -> {
                  Msg.sendInfo(player, "You have been teleported to {}.", warp.getName());
                });
                player.closeInventory();
                UtilPlayer.playUIClick(player);
              } else {
                ItemStack one = cursor.asOne();
                warp.setIcon(one);
                UtilPlayer.playUIClick(player);
                this.decorate();
              }
            })
            .asyncCreated(false)
            .build();
  }

  @Override
  public void onBottomClick(InventoryClickEvent event) {

  }
}
