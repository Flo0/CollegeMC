package net.collegemc.mc.libs.skinclient;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.collegemc.common.GlobalGateway;
import net.collegemc.common.gson.GsonSerializer;
import net.collegemc.common.model.GlobalDataObject;
import net.collegemc.mc.libs.CollegeLibrary;
import org.mineskin.MineskinClient;
import org.mineskin.SkinOptions;
import org.mineskin.Variant;
import org.mineskin.Visibility;
import org.mineskin.data.Skin;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PlayerSkinManager {

  private static final String NAMESPACE = "Skin-Cache";
  private static final String USER_AGENT = "CoreAgent";
  private static final String API_KEY = "4e4d5e9f0d61a084e0673f99f49fd182280fb670151209f46fdc5c2a38867fdb";
  private static final String KEY_SECRET = "4ee3343aec34213a2df5616b137c6a36f4a0e89884b9b3b4852019b7faa33c4d912d9cd89c78c4b968d0b78d34b0c35d9025b8f7d7da4a78c2ca131ff5c05528";

  public PlayerSkinManager() {
    GsonSerializer serializer = CollegeLibrary.getGsonSerializer();
    this.mineskinClient = new MineskinClient(USER_AGENT, API_KEY);
    this.globalObject = GlobalGateway.getDataDomainManager().getOrCreateGlobalObject(new PlayerSkinData(), serializer, GlobalGateway.DATABASE_NAME, NAMESPACE);
    this.playerSkinData = this.globalObject.getOrCreateRealTimeData();
  }

  @Getter
  private final PlayerSkinData playerSkinData;
  private final MineskinClient mineskinClient;
  private final GlobalDataObject<PlayerSkinData> globalObject;

  public Skin getSkin(String skinName) {
    return this.playerSkinData.getSkin(skinName);
  }

  public void requestNamedSkin(final String skinName, final File imageFile, final boolean scale, final Consumer<Skin> skinConsumer) {
    Integer id = this.playerSkinData.getSkinId(skinName);

    if (id == null) {
      if (scale) {
        this.uploadAndScaleHeadImage(imageFile, skinName, this.playerSkinData::addSkin);
      } else {
        this.uploadImage(imageFile, skinName, this.playerSkinData::addSkin);
      }
    }

    id = this.playerSkinData.getSkinId(skinName);

    if (id == null) {
      return;
    }

    this.requestSkin(id, skinConsumer);
  }

  public void requestSkin(final int id, final Consumer<Skin> skinConsumer) {
    Skin skin = this.playerSkinData.getSkin(id);

    final long unixWeek = TimeUnit.DAYS.toMillis(7);
    final long unixDay = unixWeek / 7;

    if (skin == null) {
      CollegeLibrary.getInstance().getLogger().info("§7Downloading Skin with ID [" + id + "] from Mineskin.org");
      skin = this.mineskinClient.getId(id).join();
      skin.timestamp = System.currentTimeMillis();
      this.playerSkinData.addSkin(skin);
      Skin finalSkin = skin;
      this.globalObject.apply(data -> data.addSkin(finalSkin));
    } else if (skin.timestamp + unixWeek + ThreadLocalRandom.current().nextLong(-unixDay, unixDay) < System.currentTimeMillis()) {
      CollegeLibrary.getInstance().getLogger().info("Skin with ID [" + id + "] has old cache data. Downloading.");
      skin = this.mineskinClient.getId(id).join();
      skin.timestamp = System.currentTimeMillis();
      this.playerSkinData.addSkin(skin);
      Skin finalSkin = skin;
      this.globalObject.apply(data -> data.addSkin(finalSkin));
    }

    CollegeLibrary.getInstance().getLogger().info("Getting Skin with ID [" + id + "] from skin cache.");
    skinConsumer.accept(skin);
  }

  public void uploadImage(final File imageFile, final String name, final Consumer<Skin> skinConsumer) {
    try {
      skinConsumer.accept(this.mineskinClient.generateUpload(imageFile, SkinOptions.create(name, Variant.AUTO, Visibility.PRIVATE)).join());
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public void uploadHeadImage(final File imageFile, final String name, final Consumer<Skin> skinConsumer) {
    final BufferedImage headImage;
    try {
      headImage = ImageIO.read(imageFile);
    } catch (final IOException e) {
      e.printStackTrace();
      return;
    }
    Preconditions.checkArgument(headImage.getWidth() == 8 && headImage.getHeight() == 8);
    final BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
    final int[] xHeadOffsets = new int[]{8, 16, 0, 8, 16, 24};
    final int[] yHeadOffsets = new int[]{0, 0, 8, 8, 8, 8, 8};
    for (int hx = 0; hx < 8; hx++) {
      for (int hy = 0; hy < 8; hy++) {
        final int rgb = headImage.getRGB(hx, hy);
        for (int index = 0; index < 6; index++) {
          final int x = xHeadOffsets[index] + hx;
          final int y = yHeadOffsets[index] + hy;
          image.setRGB(x, y, rgb);
        }
      }
    }
    final File uploadFile = new File(imageFile.getParent(), imageFile.getName().replace(".png", "") + "_scaled.png");
    try {
      ImageIO.write(image, "png", uploadFile);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    this.uploadImage(uploadFile, name, skinConsumer);
  }

  public void uploadAndScaleHeadImage(final File imageFile, final String name, final Consumer<Skin> skinConsumer) {
    final BufferedImage headImage;
    try {
      headImage = ImageIO.read(imageFile);
    } catch (final IOException e) {
      e.printStackTrace();
      return;
    }
    final double widthScale = 8D / (double) headImage.getWidth();
    final double heightScale = 8D / (double) headImage.getWidth();
    final BufferedImage image = this.scale(headImage, widthScale, heightScale);
    final File uploadFile = new File(imageFile.getParent(), imageFile.getName().replace(".png", "") + "_8.png");
    try {
      ImageIO.write(image, "png", uploadFile);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    this.uploadHeadImage(uploadFile, name, skinConsumer);
  }

  private BufferedImage scale(final BufferedImage before, final double scaleWidth, final double scaleHeight) {
    final int w = before.getWidth();
    final int h = before.getHeight();
    BufferedImage after = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    final AffineTransform at = new AffineTransform();
    at.scale(scaleWidth, scaleHeight);
    final AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
    after = scaleOp.filter(before, after);
    return after.getSubimage(0, 0, (int) (w * scaleWidth + 0.5D), ((int) (h * scaleHeight + 0.5D)));
  }

}
