package net.collegemc.common.mineskin.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Skin {
  private int id;
  private String uuid;
  private String name;
  private SkinData data;
  private long timestamp;
  @SerializedName("private")
  private boolean prvate;
  private int views;
  private int accountId;
  private int duration;
  /**
   * @deprecated
   */
  @Deprecated
  private double nextRequest;

  public Skin() {
  }

  public long getAge() {
    return System.currentTimeMillis() - this.timestamp;
  }

  public UUID getUniqueId() {
    String withDashes = this.uuid.substring(0, 8) + "-" + this.uuid.substring(8, 12) + "-" + this.uuid.substring(12, 16) + "-" + this.uuid.substring(16, 20) + "-" + this.uuid.substring(20);
    return UUID.fromString(withDashes);
  }
}
