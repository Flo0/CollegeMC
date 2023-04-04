package net.collegemc.mc.libs.skinclient;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import net.collegemc.common.mineskin.data.Skin;
import net.collegemc.mc.libs.CollegeLibrary;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@AllArgsConstructor
public enum DebugSkin {

  SPLIT_HAIR("b7544f4da91145218083c7c145296467"),
  HIPSTER("8a1aef9603794449a94c07d0c62eb571"),
  EDGY("db271ba373ab45169497f22b006536bf"),
  BANDIT("09e11d1556ad45c8a9316fef3271bcbc"),
  BUSINESS("ff02fd86979a49e29c798b5c89be58b4"),
  KING("f6d598f6c899410f8d74a92939abdc78"),
  GENERAL("e00054ff26794d93847c1c8b5fa8c365");


  private final String id;

  public CompletableFuture<Skin> get() {
    CompletableFuture<Skin> future = new CompletableFuture<>();
    CollegeLibrary.getPlayerSkinManager().requestSkin(id, future::complete);
    return future;
  }

  public void applyTo(Consumer<Skin> skinConsumer) {
    CollegeLibrary.getPlayerSkinManager().requestSkin(id, skinConsumer);
  }

  public static List<Skin> select(int amount) {
    Preconditions.checkArgument(amount <= values().length, "Cannot select more skins than there are");
    return List.of(values()).subList(0, amount).stream().map(DebugSkin::get).map(CompletableFuture::join).toList();
  }

}
