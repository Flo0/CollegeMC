package net.collegemc.mc.libs.messaging;


import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

import java.util.function.Function;
import java.util.function.Supplier;

public class Msg {

  private static final char ELEMENT_START = '{';
  private static final char ELEMENT_END = '}';
  private static final Supplier<TextComponent> errorPrefixSup = () -> Component.text("Error")
          .color(TextColor.color(230, 0, 0))
          .append(Component.text(" >> ").color(NamedTextColor.WHITE));
  private static final Supplier<TextComponent> adminPrefixSup = () -> Component.text("Admin")
          .color(TextColor.color(110, 102, 204))
          .append(Component.text(" >> ").color(NamedTextColor.WHITE));
  private static final Function<TextComponent, TextComponent> normalMessageMod = component -> component.color(NamedTextColor.GRAY);
  private static final Function<TextComponent, TextComponent> errorMessageMod = component -> component.color(TextColor.color(255, 102, 102));
  private static final Function<TextComponent, TextComponent> warningMessageMod = component -> component.color(NamedTextColor.GRAY);
  private static final Function<TextComponent, TextComponent> elementMessageMod = component -> component.color(TextColor.color(255, 225, 77));
  @Setter
  private static String serverPrefix = "UNKNOWN";
  private static final Supplier<TextComponent> normalPrefixSup = () -> Component.text(serverPrefix)
          .color(TextColor.color(0, 102, 204))
          .append(Component.text(" >> ").color(NamedTextColor.WHITE));
  private static final Supplier<TextComponent> warningPrefixSup = () -> Component.text(serverPrefix)
          .color(TextColor.color(240, 40, 0))
          .append(Component.text(" >> ").color(NamedTextColor.WHITE));

  public static void sendAdminInfo(final CommandSender target, final String message, final Object... elements) {
    sendFormatMessage(target, message, adminPrefixSup, normalMessageMod, elements);
  }

  public static void sendAdminInfo(final CommandSender target, final TextComponent component) {
    sendComponentMessage(target, component, adminPrefixSup);
  }

  public static void sendInfo(final CommandSender target, final String message, final Object... elements) {
    sendFormatMessage(target, message, normalPrefixSup, normalMessageMod, elements);
  }

  public static void sendInfo(final CommandSender target, final TextComponent component) {
    sendComponentMessage(target, component, normalPrefixSup);
  }

  public static void sendWarning(final CommandSender target, final String message, final Object... elements) {
    sendFormatMessage(target, message, warningPrefixSup, warningMessageMod, elements);
  }

  public static void sendWarning(final CommandSender target, final TextComponent component) {
    sendComponentMessage(target, component, warningPrefixSup);
  }

  public static void sendError(final CommandSender target, final String message, final Object... elements) {
    sendFormatMessage(target, message, errorPrefixSup, errorMessageMod, elements);
  }

  public static void sendError(final CommandSender target, final TextComponent component) {
    sendComponentMessage(target, component, errorPrefixSup);
  }

  private static void sendComponentMessage(
          final CommandSender target,
          final TextComponent component,
          final Supplier<TextComponent> prefixSup) {
    target.sendMessage(prefixSup.get().append(component));
  }

  private static void sendFormatMessage(
          final CommandSender target,
          final String message,
          final Supplier<TextComponent> prefixSup,
          final Function<TextComponent, TextComponent> messageMod,
          final Object[] elements
  ) {
    TextComponent component = Component.text("");
    component = component.append(prefixSup.get());
    int elementIndex = 0;
    StringBuilder currentLine = new StringBuilder();
    for (int index = 0; index < message.length(); index++) {
      final char currentChar = message.charAt(index);
      if (index + 1 < message.length() && currentChar == ELEMENT_START && message.charAt(index + 1) == ELEMENT_END) {
        TextComponent messageComponent = messageMod.apply(Component.text(currentLine.toString()));
        TextComponent objectComponent = elementMessageMod.apply(Component.text(elements[elementIndex++].toString()));
        component = component.append(messageComponent);
        component = component.append(objectComponent);
        currentLine = new StringBuilder();
        index++;
      } else {
        currentLine.append(currentChar);
      }
    }
    TextComponent messageComponent = messageMod.apply(Component.text(currentLine.toString()));
    component = component.append(messageComponent);
    target.sendMessage(component);
  }

}