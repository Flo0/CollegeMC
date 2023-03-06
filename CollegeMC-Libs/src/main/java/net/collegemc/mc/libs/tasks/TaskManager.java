package net.collegemc.mc.libs.tasks;

import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TaskManager {

  private static final BukkitScheduler scheduler = Bukkit.getScheduler();

  private static JavaPlugin plugin() {
    return CollegeLibrary.getInstance();
  }

  public static <T> Consumer<T> consumeSync(Consumer<T> consumer) {
    return (t) -> runTask(() -> consumer.accept(t));
  }

  public static <T> Supplier<T> supplySync(Supplier<T> supplier) {
    return () -> {
      CompletableFuture<T> future = new CompletableFuture<>();
      runTask(() -> future.complete(supplier.get()));
      return future.join();
    };
  }

  public static <T, E> Function<T, E> computeSync(Function<T, E> function) {
    return (t) -> {
      CompletableFuture<E> future = new CompletableFuture<>();
      runTask(() -> future.complete(function.apply(t)));
      return future.join();
    };
  }

  public static <T> Future<T> callMethodSync(Callable<T> callable) {
    return scheduler.callSyncMethod(plugin(), callable);
  }

  public static BukkitTask runTask(Runnable runnable) {
    return scheduler.runTask(plugin(), runnable);
  }

  public static BukkitTask runTaskLater(Runnable runnable, long delay) {
    return scheduler.runTaskLater(plugin(), runnable, delay);
  }

  public static BukkitTask runTaskTimer(Runnable runnable, long delay, long repeat) {
    return scheduler.runTaskTimer(plugin(), runnable, delay, repeat);
  }

}
