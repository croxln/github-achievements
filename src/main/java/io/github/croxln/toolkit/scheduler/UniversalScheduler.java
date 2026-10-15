package io.github.croxln.toolkit.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Universal abstraction layer for task execution that works seamlessly on both
 * traditional Paper servers and multi-threaded Folia environments.
 */
public final class UniversalScheduler {

    private static final boolean IS_FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;
    }

    private UniversalScheduler() {}

    /**
     * Checks if the current server environment is running on Folia.
     */
    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Runs a synchronous task on the main/global thread.
     */
    @NotNull
    public static TaskHandle runSync(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        if (IS_FOLIA) {
            ScheduledTask task = Bukkit.getGlobalRegionScheduler().run(plugin, t -> runnable.run());
            return new FoliaTaskHandle(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTask(plugin, runnable);
            return new BukkitTaskHandle(task);
        }
    }

    /**
     * Runs an asynchronous task in the server's async pool.
     */
    @NotNull
    public static TaskHandle runAsync(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        if (IS_FOLIA) {
            ScheduledTask task = Bukkit.getAsyncScheduler().runNow(plugin, t -> runnable.run());
            return new FoliaTaskHandle(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
            return new BukkitTaskHandle(task);
        }
    }

    /**
     * Runs a synchronous task after a specified delay in ticks.
     */
    @NotNull
    public static TaskHandle runLaterSync(@NotNull Plugin plugin, @NotNull Runnable runnable, long delayTicks) {
        if (delayTicks <= 0) {
            return runSync(plugin, runnable);
        }
        if (IS_FOLIA) {
            ScheduledTask task = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> runnable.run(), delayTicks);
            return new FoliaTaskHandle(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
            return new BukkitTaskHandle(task);
        }
    }

    /**
     * Runs an asynchronous task after a specified delay in ticks.
     */
    @NotNull
    public static TaskHandle runLaterAsync(@NotNull Plugin plugin, @NotNull Runnable runnable, long delayTicks) {
        if (IS_FOLIA) {
            long delayMillis = delayTicks * 50L;
            ScheduledTask task = Bukkit.getAsyncScheduler().runDelayed(plugin, t -> runnable.run(), delayMillis, TimeUnit.MILLISECONDS);
            return new FoliaTaskHandle(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks);
            return new BukkitTaskHandle(task);
        }
    }

    /**
     * Runs a repeating synchronous task at fixed rate in ticks.
     */
    @NotNull
    public static TaskHandle runTimerSync(@NotNull Plugin plugin, @NotNull Runnable runnable, long initialDelayTicks, long periodTicks) {
        long delay = Math.max(1, initialDelayTicks);
        long period = Math.max(1, periodTicks);
        if (IS_FOLIA) {
            ScheduledTask task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> runnable.run(), delay, period);
            return new FoliaTaskHandle(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period);
            return new BukkitTaskHandle(task);
        }
    }

    /**
     * Runs a repeating asynchronous task.
     */
    @NotNull
    public static TaskHandle runTimerAsync(@NotNull Plugin plugin, @NotNull Runnable runnable, long initialDelayTicks, long periodTicks) {
        long delayMillis = Math.max(50L, initialDelayTicks * 50L);
        long periodMillis = Math.max(50L, periodTicks * 50L);
        if (IS_FOLIA) {
            ScheduledTask task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, t -> runnable.run(), delayMillis, periodMillis, TimeUnit.MILLISECONDS);
            return new FoliaTaskHandle(task);
        } else {
            BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, initialDelayTicks, periodTicks);
            return new BukkitTaskHandle(task);
        }
    }

    /**
     * Runs a task bounded to a specific world location (Region scheduler on Folia).
     */
    public static void runAtLocation(@NotNull Plugin plugin, @NotNull Location location, @NotNull Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().execute(plugin, location, runnable);
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Runs a task bounded to a specific Entity context.
     */
    public static void runAtEntity(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable runnable) {
        if (IS_FOLIA) {
            entity.getScheduler().run(plugin, t -> runnable.run(), null);
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    private static final class FoliaTaskHandle implements TaskHandle {
        private final ScheduledTask task;

        private FoliaTaskHandle(ScheduledTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }

    private static final class BukkitTaskHandle implements TaskHandle {
        private final BukkitTask task;

        private BukkitTaskHandle(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }
}
