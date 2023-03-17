package net.collegemc.mc.libs.regions.permissions;

import net.collegemc.mc.libs.gui.abstraction.GuiButton;
import net.collegemc.mc.libs.gui.baseimpl.DynamicGui;
import net.collegemc.mc.libs.regions.AbstractRegion;
import net.collegemc.mc.libs.spigot.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import reactor.core.publisher.Mono;

import java.util.List;

public class RegionPermissionGUI extends DynamicGui {

  private final AbstractRegion region;

  public RegionPermissionGUI(AbstractRegion region) {
    this.region = region;
  }

  @Override
  protected void setupButtons() {
    for (RegionPermission permission : RegionPermission.values()) {
      this.addButton(this.createPermissionButton(permission));
    }
  }

  private GuiButton createPermissionButton(RegionPermission permission) {
    return GuiButton.builder()
            .asyncCreated(false)
            .iconCreator(Mono.fromSupplier(() -> ItemBuilder.of(permission.getDisplayIcon())
                    .name("§e" + this.region.getName())
                    .lore("", "§fUsers:")
                    .lore(this.getUserNames(permission))
                    .build()))
            .eventConsumer(event -> {

            }).build();
  }

  private List<TextComponent> getUserNames(RegionPermission permission) {
    return this.region.getPermissionContainer().getAllowedUsersFor(permission).stream()
            .map(Bukkit::getOfflinePlayer)
            .map(OfflinePlayer::getName)
            .map(name -> Component.text("§f- §7" + name))
            .toList();
  }

  @Override
  protected Inventory createInventory() {
    return Bukkit.createInventory(null, 9 * 3, Component.text(this.region.getName() + " Perms"));
  }
}
