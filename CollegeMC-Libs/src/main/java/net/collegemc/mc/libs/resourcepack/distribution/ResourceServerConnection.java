package net.collegemc.mc.libs.resourcepack.distribution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceServerConnection implements Runnable {

  private static final Pattern GET_REQUEST_PATTERN = Pattern.compile("GET /?(\\S*).*");

  protected final ResourcePackServer server;
  protected final Socket client;

  public ResourceServerConnection(final ResourcePackServer server, final Socket client) {
    this.server = server;
    this.client = client;
  }

  public Socket getClient() {
    return this.client;
  }

  @Override
  public void run() {
    try {
      final BufferedReader in = new BufferedReader(new InputStreamReader(this.client.getInputStream(), StandardCharsets.ISO_8859_1));
      final OutputStream out = this.client.getOutputStream();
      final PrintWriter pout = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.ISO_8859_1), true);
      String request = in.readLine();
      this.server.onClientRequest(this, request);

      final Matcher getMatcher = GET_REQUEST_PATTERN.matcher(request);
      if (getMatcher.matches()) {
        request = getMatcher.group(1);
        final File result = this.server.requestFileCallback(this, request);
        if (result == null) {
          pout.println("HTTP/1.0 400 Bad Request");
          this.server.onRequestError(this, 400);
        } else {
          try (final FileInputStream fis = new FileInputStream(result)) {
            // Writes zip files specifically;
            out.write("HTTP/1.0 200 OK\r\n".getBytes());
            out.write("Content-Type: application/zip\r\n".getBytes());
            out.write(("Content-Length: " + result.length() + "\r\n").getBytes());
            out.write(("Date: " + new Date().toInstant() + "\r\n").getBytes());
            out.write("Server: Httpd\r\n\r\n".getBytes());
            final byte[] data = new byte[64 * 1024];
            for (int read; (read = fis.read(data)) > -1; ) {
              out.write(data, 0, read);
            }
            out.flush();
            this.server.onSuccessfulRequest(this, request);
          } catch (final FileNotFoundException e) {
            pout.println("HTTP/1.0 404 Object Not Found");
            this.server.onRequestError(this, 404);
          }
        }
      } else {
        pout.println("HTTP/1.0 400 Bad Request");
        this.server.onRequestError(this, 400);
      }
      this.client.close();
    } catch (final IOException e) {
      System.out.println("Oh no, it's broken D: " + e);
    }
  }
}
