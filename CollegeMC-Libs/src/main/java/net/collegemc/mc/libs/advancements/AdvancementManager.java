package net.collegemc.mc.libs.advancements;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdvancementManager {

  private static final String criterionId = "collegemc:criterion";
  private static final ResourceLocation identifier = ResourceLocation.of("collegemc:notification", ':');
  private static final Packet<?> removePacket = createRemovePacket();

  public static void sendAdvancement(Player player, org.bukkit.inventory.ItemStack icon, String title, FrameType frameType) {
    sendAdvancement(player, CraftItemStack.asNMSCopy(icon), Component.literal(title), frameType);
  }

  public static void sendAdvancement(Player player, ItemStack icon, Component title, FrameType frameType) {
    DisplayInfo displayInfo = new DisplayInfo(icon, title, Component.literal(""), null, frameType.getNmsType(), true, true, false);

    Criterion criterion = new Criterion();

    AdvancementRewards rewards = AdvancementRewards.EMPTY;
    String[][] requirements = {{criterionId}};
    Advancement advancement = new Advancement(identifier, null, displayInfo, rewards, Map.of(criterionId, criterion), requirements);

    AdvancementProgress progress = new AdvancementProgress();
    progress.update(Map.of(criterionId, criterion), requirements);
    progress.grantProgress(criterionId);

    List<Advancement> advancements = List.of(advancement);
    Map<ResourceLocation, AdvancementProgress> progressMap = Map.of(identifier, progress);
    ClientboundUpdateAdvancementsPacket packet = new ClientboundUpdateAdvancementsPacket(false, advancements, Set.of(), progressMap);

    ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
    connection.send(packet);
    connection.send(removePacket);
  }

  private static Packet<?> createRemovePacket() {
    return new ClientboundUpdateAdvancementsPacket(false, List.of(), Set.of(identifier), Map.of());
  }

  @AllArgsConstructor
  public enum FrameType {
    TASK(net.minecraft.advancements.FrameType.TASK),
    CHALLENGE(net.minecraft.advancements.FrameType.CHALLENGE),
    GOAL(net.minecraft.advancements.FrameType.GOAL);

    @Getter
    private net.minecraft.advancements.FrameType nmsType;
  }

  private static class NotifyTrigger implements CriterionTriggerInstance {
    private static final ResourceLocation identifier = ResourceLocation.of(criterionId, ':');

    @Override
    public @NotNull ResourceLocation getCriterion() {
      return identifier;
    }

    @Override
    public @NotNull JsonObject serializeToJson(@NotNull SerializationContext predicateSerializer) {
      return new JsonObject();
    }
  }

}
