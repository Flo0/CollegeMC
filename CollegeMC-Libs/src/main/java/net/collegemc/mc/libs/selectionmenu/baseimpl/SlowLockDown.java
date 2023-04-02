package net.collegemc.mc.libs.selectionmenu.baseimpl;

import net.collegemc.mc.libs.selectionmenu.TieDown;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class SlowLockDown implements TieDown {

  private static final String MOD_ID = "2bf77aa6-f153-4f44-9219-c094951cf0b8";
  private static final String MOD_NAME = "_SELECTION_LOCK_";
  private final AttributeModifier modifier;
  private final PotionEffect jumpEffect;

  public SlowLockDown() {
    UUID uid = UUID.fromString(MOD_ID);
    this.modifier = new AttributeModifier(uid, MOD_NAME, -1D, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    this.jumpEffect = new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 256, false, false);
  }

  @Override
  public void tieDown(Player player, Location location) {
    player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(this.modifier);
    player.addPotionEffect(this.jumpEffect);
  }

  @Override
  public void release(Player player) {
    player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).removeModifier(this.modifier);
    player.removePotionEffect(this.jumpEffect.getType());
  }
}
