package net.collegemc.common.mineskin;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.collegemc.common.mineskin.data.Skin;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MineSkinClient {
  private static final String API_BASE = "https://api.mineskin.org";
  private static final String GENERATE_BASE = API_BASE + "/generate";
  private static final String GET_BASE = API_BASE + "/get";
  private static final String ID_FORMAT = "https://api.mineskin.org/get/id/%s";
  private static final String URL_FORMAT = "https://api.mineskin.org/generate/url?url=%s&%s";
  private static final String UPLOAD_FORMAT = "https://api.mineskin.org/generate/upload?%s";
  private static final String USER_FORMAT = "https://api.mineskin.org/generate/user/%s?%s";
  private final Executor requestExecutor;
  private final String userAgent;
  private final String apiKey;
  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private long nextRequest = 0L;

  public MineSkinClient(String userAgent) {
    this.requestExecutor = Executors.newSingleThreadExecutor();
    this.userAgent = Preconditions.checkNotNull(userAgent);
    this.apiKey = null;
  }

  public MineSkinClient(String userAgent, String apiKey) {
    this.requestExecutor = Executors.newSingleThreadExecutor();
    this.userAgent = Preconditions.checkNotNull(userAgent);
    this.apiKey = apiKey;
  }

  public MineSkinClient(Executor requestExecutor, String userAgent, String apiKey) {
    this.requestExecutor = Preconditions.checkNotNull(requestExecutor);
    this.userAgent = Preconditions.checkNotNull(userAgent);
    this.apiKey = apiKey;
  }

  public MineSkinClient(Executor requestExecutor, String userAgent) {
    this.requestExecutor = Preconditions.checkNotNull(requestExecutor);
    this.userAgent = Preconditions.checkNotNull(userAgent);
    this.apiKey = null;
  }

  public long getNextRequest() {
    return this.nextRequest;
  }

  private Connection generateRequest(String endpoint) {
    Connection connection = Jsoup.connect(GENERATE_BASE + endpoint)
            .method(Connection.Method.POST)
            .userAgent(this.userAgent)
            .ignoreContentType(true)
            .ignoreHttpErrors(true)
            .timeout(30000);

    if (this.apiKey != null) {
      connection.header("Authorization", "Bearer " + this.apiKey);
    }

    return connection;
  }

  private Connection getRequest(String endpoint) {
    return Jsoup.connect(GET_BASE + endpoint)
            .method(Connection.Method.GET)
            .userAgent(this.userAgent)
            .ignoreContentType(true)
            .ignoreHttpErrors(true)
            .timeout(5000);
  }

  public CompletableFuture<Skin> getId(long id) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        Connection connection = this.getRequest("/id/" + id);
        return this.handleResponse(connection.execute().body());
      } catch (Exception exception) {
        throw new RuntimeException(exception);
      }
    }, this.requestExecutor);
  }

  public CompletableFuture<Skin> getUuid(UUID uuid) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        Connection connection = this.getRequest("/uuid/" + uuid);
        return this.handleResponse(connection.execute().body());
      } catch (Exception exception) {
        throw new RuntimeException(exception);
      }
    }, this.requestExecutor);
  }

  public CompletableFuture<Skin> generateUrl(String url) {
    return this.generateUrl(url, SkinOptions.none());
  }

  public CompletableFuture<Skin> generateUrl(String url, SkinOptions options) {
    Preconditions.checkNotNull(url);
    Preconditions.checkNotNull(options);
    return CompletableFuture.supplyAsync(() -> {
      try {
        if (System.currentTimeMillis() < this.nextRequest) {
          long delay = this.nextRequest - System.currentTimeMillis();
          Thread.sleep(delay + 1000L);
        }

        JsonObject body = options.toJson();
        body.addProperty("url", url);
        Connection connection = this.generateRequest("/url")
                .header("Content-Type", "application/json")
                .requestBody(body.toString());

        return this.handleResponse(connection.execute().body());
      } catch (Exception exception) {
        throw new RuntimeException(exception);
      }
    }, this.requestExecutor);
  }

  public CompletableFuture<Skin> generateUpload(InputStream is) {
    return this.generateUpload(is, SkinOptions.none(), null);
  }

  public CompletableFuture<Skin> generateUpload(InputStream is, SkinOptions options) {
    return this.generateUpload(is, options, options.getName() + ".png");
  }

  public CompletableFuture<Skin> generateUpload(InputStream is, String name) {
    return this.generateUpload(is, SkinOptions.none(), name);
  }

  public CompletableFuture<Skin> generateUpload(InputStream is, SkinOptions options, String name) {
    Preconditions.checkNotNull(is);
    Preconditions.checkNotNull(options);
    return CompletableFuture.supplyAsync(() -> {
      try {
        if (System.currentTimeMillis() < this.nextRequest) {
          long delay = this.nextRequest - System.currentTimeMillis();
          Thread.sleep(delay + 1000L);
        }

        Connection connection = this.generateRequest("/upload").data("file", name, is);
        options.addAsData(connection);
        return this.handleResponse(connection.execute().body());
      } catch (Exception exception) {
        throw new RuntimeException(exception);
      }
    }, this.requestExecutor);
  }

  public CompletableFuture<Skin> generateUpload(File file) throws FileNotFoundException {
    return this.generateUpload(file, SkinOptions.none());
  }

  public CompletableFuture<Skin> generateUpload(File file, SkinOptions options) throws FileNotFoundException {
    Preconditions.checkNotNull(file);
    Preconditions.checkNotNull(options);
    return this.generateUpload((new FileInputStream(file)), options, file.getName());
  }

  public CompletableFuture<Skin> generateUpload(RenderedImage image) throws IOException {
    return this.generateUpload(image, SkinOptions.none());
  }

  public CompletableFuture<Skin> generateUpload(RenderedImage image, SkinOptions options) throws IOException {
    Preconditions.checkNotNull(image);
    Preconditions.checkNotNull(options);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    return this.generateUpload(new ByteArrayInputStream(baos.toByteArray()), options);
  }

  public CompletableFuture<Skin> generateUser(UUID uuid) {
    return this.generateUser(uuid, SkinOptions.none());
  }

  public CompletableFuture<Skin> generateUser(UUID uuid, SkinOptions options) {
    Preconditions.checkNotNull(uuid);
    Preconditions.checkNotNull(options);
    return CompletableFuture.supplyAsync(() -> {
      try {
        if (System.currentTimeMillis() < this.nextRequest) {
          long delay = this.nextRequest - System.currentTimeMillis();
          Thread.sleep(delay + 1000L);
        }

        JsonObject body = options.toJson();
        body.addProperty("uuid", uuid.toString());
        Connection connection = this.generateRequest("/user")
                .header("Content-Type", "application/json")
                .requestBody(body.toString());

        return this.handleResponse(connection.execute().body());
      } catch (Exception exception) {
        throw new RuntimeException(exception);
      }
    }, this.requestExecutor);
  }

  Skin handleResponse(String body) throws IOException {
    JsonObject jsonObject = this.gson.fromJson(body, JsonObject.class);
    if (jsonObject.has("error")) {
      throw new IOException(jsonObject.get("error").getAsString());
    } else {
      Skin skin = this.gson.fromJson(jsonObject, Skin.class);
      this.nextRequest = System.currentTimeMillis() + (long) (skin.getDuration() + (this.apiKey == null ? 10000 : 100));
      return skin;
    }
  }
}
