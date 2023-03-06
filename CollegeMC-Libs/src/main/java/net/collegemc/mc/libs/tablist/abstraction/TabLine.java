package net.collegemc.mc.libs.tablist.abstraction;

import net.minecraft.server.network.ServerGamePacketListenerImpl;

public interface TabLine {

  void setDisplay(String display);

  String getDisplay();

  void setTexture(String texture, String signature);

  void setTextureBase64(String textureBase64);

  void send(ServerGamePacketListenerImpl connection);

  void sendDisplayUpdate(ServerGamePacketListenerImpl connection);

  void sendProfileUpdate(ServerGamePacketListenerImpl connection);

  void sendHide(ServerGamePacketListenerImpl connection);

}