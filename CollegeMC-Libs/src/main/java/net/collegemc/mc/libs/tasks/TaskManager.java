package net.collegemc.mc.libs.tasks;

import net.collegemc.mc.libs.CollegeLibrary;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TaskManager {

  private static final double TIMEOUT_PERCENTAGE_LENIENCY = 1.1;
  private static final BukkitScheduler scheduler = Bukkit.getScheduler();
  private static final Executor IOExecutor = Executors.newFixedThreadPool(4);
  private static final Executor computationPool = Executors.newFixedThreadPool(2);

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

  public static BukkitTask runTask(BukkitRunnable runnable) {
    return runnable.runTask(plugin());
  }

  public static BukkitTask runTaskLater(BukkitRunnable runnable, long delay) {
    return runnable.runTaskLater(plugin(), delay);
  }

  public static BukkitTask runTaskTimer(BukkitRunnable runnable, long delay, long repeatDelay) {
    return runnable.runTaskTimer(plugin(), delay, repeatDelay);
  }

  public static BukkitTask runTaskAsync(BukkitRunnable runnable) {
    return runnable.runTaskAsynchronously(plugin());
  }

  public static BukkitTask runTaskLaterAsync(BukkitRunnable runnable, long delay) {
    return runnable.runTaskLaterAsynchronously(plugin(), delay);
  }

  public static BukkitTask runTaskTimerAsync(BukkitRunnable runnable, long delay, long repeatDelay) {
    return runnable.runTaskTimerAsynchronously(plugin(), delay, repeatDelay);
  }

  public static BukkitTask runTask(Runnable runnable) {
    return scheduler.runTask(plugin(), runnable);
  }

  public static BukkitTask runTaskLater(Runnable runnable, long delay) {
    return scheduler.runTaskLater(plugin(), runnable, delay);
  }

  public static BukkitTask runTaskTimer(Runnable runnable, long delay, long repeatDelay) {
    return scheduler.runTaskTimer(plugin(), runnable, delay, repeatDelay);
  }

  public static CompletableFuture<Void> tickDelayedFuture(int ticks) {
    return runOnComputationPool(() -> {
      CountDownLatch latch = new CountDownLatch(1);
      TaskManager.runTaskLater(latch::countDown, ticks);
      try {
        long seconds = (long) ((20L * ticks) * TIMEOUT_PERCENTAGE_LENIENCY);
        if (!latch.await(seconds, TimeUnit.SECONDS)) {
          throw new RuntimeException("Unexpected timeout.");
        }
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
  }

  public static <T> CompletableFuture<T> supplyOnIOPool(Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier, IOExecutor).whenComplete(TaskManager::handleResult);
  }

  public static CompletableFuture<Void> runOnIOPool(Runnable runnable) {
    return CompletableFuture.runAsync(runnable, IOExecutor).whenComplete(TaskManager::handleResult);
  }

  public static <T> CompletableFuture<T> supplyOnComputationPool(Supplier<T> supplier) {
    return CompletableFuture.supplyAsync(supplier, computationPool).whenComplete(TaskManager::handleResult);
  }

  public static CompletableFuture<Void> runOnComputationPool(Runnable runnable) {
    return CompletableFuture.runAsync(runnable, computationPool).whenComplete(TaskManager::handleResult);
  }

  private static <T> void handleResult(T value, Throwable throwable) {
    if (throwable != null) {
      throwable.printStackTrace();
    }
  }

}
