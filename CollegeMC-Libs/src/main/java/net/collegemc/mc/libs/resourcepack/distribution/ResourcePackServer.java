package net.collegemc.mc.libs.resourcepack.distribution;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class ResourcePackServer extends Thread {

  private volatile boolean running = true;

  protected final int port;
  protected final ServerSocket serverSocket;

  public ResourcePackServer(final int port) throws IOException {
    this.port = port;
    this.serverSocket = new ServerSocket(port);
    this.serverSocket.setReuseAddress(true);
  }

  private void spawnConnectionThread() throws IOException {
    final Socket connectionSocket = this.serverSocket.accept();
    final ResourceServerConnection connection = new ResourceServerConnection(this, connectionSocket);
    new Thread(connection).start();
  }

  @Override
  public void run() {
    try {
      while (this.running) {
        this.spawnConnectionThread();
      }

      if (!this.serverSocket.isClosed()) {
        this.serverSocket.close();
      }
    } catch (final IOException e) {
      Bukkit.getLogger().info("The resourcepack http thread was interrupted!");
      // e.printStackTrace();
    }
  }

  public void terminate() {
    this.running = false;
    if (!this.serverSocket.isClosed()) {
      try {
        this.serverSocket.close();
      } catch (final IOException e) {
        e.printStackTrace();
      }
    }
  }

  public File requestFileCallback(final ResourceServerConnection connection, final String request) {
    return null;
  }

  public abstract void onSuccessfulRequest(final ResourceServerConnection connection, final String request);

  public abstract void onClientRequest(final ResourceServerConnection connection, final String request);

  public abstract void onRequestError(final ResourceServerConnection connection, final int code);

}