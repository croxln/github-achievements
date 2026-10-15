package io.github.croxln.toolkit.thread;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Universal thread bridge for Leaf, Folia, and Paper.
 * Guarantees every player, entity, and block operation runs strictly on the thread that owns it,
 * preventing regional threading violations while automatically tracking and cancelling tasks on reload/disable.
 */
public final class ThreadBridge implements AutoCloseable {

    public interface TaskHandle {
        void cancel();
    }

    private final Plugin plugin;
    private final boolean folia;
    private final Set<TaskHandle> tasks = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    public ThreadBridge(@NotNull Plugin plugin) {
        this.plugin = plugin;
        boolean found;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            found = true;
        } catch (ClassNotFoundException ignored) {
            found = false;
        }
        this.folia = found;
    }

    public boolean isFolia() {
        return folia;
    }

    /**
     * Executes action on the regional thread owning the specified player.
     */
    public void run(@NotNull Player player, @NotNull Runnable action) {
        runOrRetired(player, action, () -> {});
    }

    /**
     * Executes action on player's regional thread, or runs fallback if the player has retired (logged off / dead).
     */
    public void runOrRetired(@NotNull Player player, @NotNull Runnable action, @NotNull Runnable retired) {
        if (closed || !plugin.isEnabled()) {
            retired.run();
            return;
        }
        if (folia) {
            ScheduledTask task = player.getScheduler().run(plugin, t -> {
                if (!closed) action.run();
                else retired.run();
            }, retired);
            if (task == null) retired.run();
        } else if (Bukkit.isPrimaryThread()) {
            action.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!closed) action.run();
                else retired.run();
            });
        }
    }

    /**
     * Executes action on the regional thread owning a specific entity.
     */
    public void runEntity(@NotNull Entity entity, @NotNull Runnable action) {
        if (closed || !plugin.isEnabled()) return;
        if (folia) {
            entity.getScheduler().run(plugin, t -> {
                if (!closed) action.run();
            }, null);
        } else if (Bukkit.isPrimaryThread()) {
            action.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!closed) action.run();
            });
        }
    }

    /**
     * Executes an action on the global/main tick thread.
     */
    public void global(@NotNull Runnable action) {
        if (closed || !plugin.isEnabled()) return;
        if (folia) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> {
                if (!closed) action.run();
            });
        } else if (Bukkit.isPrimaryThread()) {
            action.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!closed) action.run();
            });
        }
    }

    /**
     * Executes an action on the regional thread owning a specific location.
     */
    public void at(@NotNull Location location, @NotNull Runnable action) {
        if (closed || !plugin.isEnabled()) return;
        if (folia) {
            Bukkit.getRegionScheduler().run(plugin, location, t -> {
                if (!closed) action.run();
            });
        } else if (Bukkit.isPrimaryThread()) {
            action.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!closed) action.run();
            });
        }
    }

    /**
     * Executes an action on the asynchronous thread pool.
     */
    public void async(@NotNull Runnable action) {
        if (closed || !plugin.isEnabled()) return;
        if (folia) {
            Bukkit.getAsyncScheduler().runNow(plugin, t -> {
                if (!closed) action.run();
            });
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                if (!closed) action.run();
            });
        }
    }

    /**
     * Schedules a repeating regional task on the player's thread.
     */
    @NotNull
    public TaskHandle repeat(@NotNull Player player, long delayTicks, long periodTicks, @NotNull Runnable action) {
        if (closed || !plugin.isEnabled()) return () -> {};
        long d = Math.max(1, delayTicks);
        long p = Math.max(1, periodTicks);
        if (folia) {
            return track(player.getScheduler().runAtFixedRate(plugin, t -> {
                if (!closed) action.run();
            }, null, d, p));
        }
        return track(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!closed) action.run();
        }, d, p));
    }

    /**
     * Schedules a delayed regional task on the player's thread.
     */
    @NotNull
    public TaskHandle later(@NotNull Player player, long delayTicks, @NotNull Runnable action) {
        if (closed || !plugin.isEnabled()) return () -> {};
        AtomicReference<TaskHandle> ref = new AtomicReference<>();
        Runnable once = () -> {
            try {
                if (!closed) action.run();
            } finally {
                TaskHandle handle = ref.get();
                if (handle != null) handle.cancel();
            }
        };
        long d = Math.max(1, delayTicks);
        TaskHandle handle = folia ? track(player.getScheduler().runDelayed(plugin, t -> once.run(), () -> {
            TaskHandle current = ref.get();
            if (current != null) current.cancel();
        }, d)) : track(Bukkit.getScheduler().runTaskLater(plugin, once, d));
        ref.set(handle);
        return handle;
    }

    /**
     * Schedules a repeating global task.
     */
    @NotNull
    public TaskHandle repeatGlobal(long delayTicks, long periodTicks, @NotNull Runnable action) {
        if (closed || !plugin.isEnabled()) return () -> {};
        long d = Math.max(1, delayTicks);
        long p = Math.max(1, periodTicks);
        if (folia) {
            return track(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> {
                if (!closed) action.run();
            }, d, p));
        }
        return track(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!closed) action.run();
        }, d, p));
    }

    private TaskHandle track(@Nullable ScheduledTask task) {
        if (task == null) return () -> {};
        TaskHandle handle = task::cancel;
        tasks.add(handle);
        return () -> {
            handle.cancel();
            tasks.remove(handle);
        };
    }

    private TaskHandle track(@Nullable BukkitTask task) {
        if (task == null) return () -> {};
        TaskHandle handle = task::cancel;
        tasks.add(handle);
        return () -> {
            handle.cancel();
            tasks.remove(handle);
        };
    }

    @Override
    public void close() {
        closed = true;
        tasks.forEach(TaskHandle::cancel);
        tasks.clear();
        if (folia) {
            Bukkit.getGlobalRegionScheduler().cancelTasks(plugin);
            Bukkit.getAsyncScheduler().cancelTasks(plugin);
        } else {
            Bukkit.getScheduler().cancelTasks(plugin);
        }
    }
}
