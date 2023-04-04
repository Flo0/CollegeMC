package net.collegemc.mc.libs.spigot;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.collegemc.common.mineskin.data.Skin;
import net.collegemc.common.mineskin.data.Texture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

public class UtilItem implements Listener {

  public static void init(final JavaPlugin plugin) {
    Bukkit.getPluginManager().registerEvents(new UtilItem(), plugin);
  }

  // TODO? One char for every ItemStack in the game?
//  public static char asChar(final ItemStack item) {
//    final NBTItem nbt = new NBTItem(item);
//    if (nbt.hasKey("Model")) {
//      return Model.valueOf(nbt.getString("Model")).getChar();
//    }
//    return 'X';
//  }

  private static final Map<String, ItemStack> nameHeadCache = new Object2ObjectOpenHashMap<>();
  private static final Map<String, ItemStack> base64HeadCache = new Object2ObjectOpenHashMap<>();
  private static final Map<UUID, ItemStack> gameProfileHeadCache = new Object2ObjectOpenHashMap<>();
  private static final Map<Skin, ItemStack> skinHeadCache = new Object2ObjectOpenHashMap<>();

  public static ItemStack getHeadFromGameProfile(final GameProfile gameProfile) {
    return UtilItem.gameProfileHeadCache.computeIfAbsent(gameProfile.getId(), pName -> UtilItem.produceHead(gameProfile)).clone();
  }

  public static ItemStack getHeadFromName(final String name) {
    return UtilItem.nameHeadCache.computeIfAbsent(name, pName -> UtilItem.produceHead(new GameProfile(UUID.randomUUID(), name))).clone();
  }

  public static ItemStack getHeadFromBase64(final String name, final String base64Data) {
    return UtilItem.base64HeadCache.computeIfAbsent(name, pName -> UtilItem.produceHead(UtilItem.createProfileWithTexture(base64Data)))
            .clone();
  }

  private static GameProfile createProfileWithTexture(final String base64Data) {
    final GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
    gameProfile.getProperties().put("textures", new Property("textures", base64Data));
    return gameProfile;
  }

  public static int getVanillaBurnDuration(final ItemStack itemStack) {
    return FurnaceBlockEntity.getFuel().get(CraftItemStack.asNMSCopy(itemStack).getItem());
  }

  public static String serialize(final ItemStack[] items) {
    try {
      final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

      // Write the size of the inventory
      dataOutput.writeInt(items.length);

      // Save every element in the list
      for (final ItemStack item : items) {
        dataOutput.writeObject(item);
      }

      // Serialize that array
      dataOutput.close();
      return Base64Coder.encodeLines(outputStream.toByteArray());
    } catch (final Exception e) {
      throw new IllegalStateException("Unable to save item stacks.", e);
    }
  }

  public static String serialize(final ItemStack itemStack) {
    final CompoundTag tag = new CompoundTag();
    CraftItemStack.asNMSCopy(itemStack).save(tag);
    return tag.toString();
  }

  public static ItemStack deserializeItemStack(final String string) {
    if (string == null || string.equals("empty")) {
      return null;
    }
    try {
      final CompoundTag comp = TagParser.parseTag(string);
      final net.minecraft.world.item.ItemStack cis = net.minecraft.world.item.ItemStack.of(comp);
      return CraftItemStack.asBukkitCopy(cis);
    } catch (final CommandSyntaxException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public static String itemStackToBase64(final ItemStack item) {
    final String line;
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
      dataOutput.writeObject(item);
      line = Base64Coder.encodeLines(outputStream.toByteArray());
    } catch (final IOException e) {
      e.printStackTrace();
      return null;
    }
    return line;
  }


  public static ItemStack[] deserialize(final String data) {
    try {
      final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
      final BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
      final ItemStack[] items = new ItemStack[dataInput.readInt()];

      // Read the serialized inventory
      for (int i = 0; i < items.length; i++) {
        items[i] = (ItemStack) dataInput.readObject();
      }

      dataInput.close();
      return items;
    } catch (final ClassNotFoundException | IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static boolean isEnachantable(final ItemStack item) {
    if (!item.getEnchantments().isEmpty()) {
      return false;
    }
    final PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
    return !container.has(NamespaceFactory.provide("ENCHANTBLOCK"), PersistentDataType.INTEGER);
  }

  public static boolean canReceiveAnvil(final ItemStack item) {
    final PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
    return !container.has(NamespaceFactory.provide("BLOCKANVILRECEIVER"), PersistentDataType.INTEGER);
  }

  public static boolean canProvideAnvil(final ItemStack item) {
    final PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
    return !container.has(NamespaceFactory.provide("BLOCKANVILPROVIDER"), PersistentDataType.INTEGER);
  }

  public static ItemStack produceHead(final PlayerProfile playerProfile) {
    final ItemStack newHead = new ItemStack(Material.PLAYER_HEAD);
    final SkullMeta headMeta = (SkullMeta) newHead.getItemMeta();
    headMeta.setPlayerProfile(playerProfile);
    newHead.setItemMeta(headMeta);
    return newHead;
  }

  public static ItemStack produceHeadProxied(final GameProfile gameProfile) {
    return produceHead(new CraftPlayerProfile(gameProfile));
  }

  public static ItemStack produceHead(final GameProfile gameProfile) {
    final ItemStack newHead = new ItemStack(Material.PLAYER_HEAD);
    final SkullMeta headMeta = (SkullMeta) newHead.getItemMeta();
    final Field profileField;

    try {
      profileField = headMeta.getClass().getDeclaredField("profile");
      profileField.setAccessible(true);
      profileField.set(headMeta, gameProfile);
    } catch (final ReflectiveOperationException e) {
      e.printStackTrace();
    }

    newHead.setItemMeta(headMeta);
    return newHead;
  }

  private UtilItem() {

  }

  public static ItemStack produceHead(Skin skin) {
    return skinHeadCache.computeIfAbsent(skin, key -> {
      PlayerProfile playerProfile = Bukkit.createProfile(key.getUniqueId(), key.getName());
      Texture texture = skin.getData().getTexture();
      playerProfile.setProperty(new ProfileProperty("textures", texture.getValue(), texture.getSignature()));
      ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta headMeta = (SkullMeta) headItem.getItemMeta();
      headMeta.setPlayerProfile(playerProfile);
      headItem.setItemMeta(headMeta);
      return headItem;
    }).clone();
  }

  @EventHandler
  public void onEnchant(final PrepareItemEnchantEvent event) {
    if (UtilItem.isEnachantable(event.getItem())) {
      return;
    }
    event.setCancelled(true);
  }

  @EventHandler
  public void onAnvil(final PrepareAnvilEvent event) {
    final ItemStack left = event.getInventory().getItem(0);
    final ItemStack right = event.getInventory().getItem(1);
    if (left == null || right == null || left.getType() == Material.AIR || right.getType() == Material.AIR) {
      return;
    }
    if (!UtilItem.canReceiveAnvil(left) || !UtilItem.canProvideAnvil(right)) {
      event.setResult(null);
    }
  }


}
