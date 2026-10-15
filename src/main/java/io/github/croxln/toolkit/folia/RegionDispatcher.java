package io.github.croxln.toolkit.folia;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Thread-safe cross-region task and state dispatcher for Folia and Paper.
 * Eliminates "Thread attempted to access entity/chunk outside of its region context" crashes.
 */
public final class RegionDispatcher {

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

    private RegionDispatcher() {}

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Executes a runnable on the region context of a specific location.
     */
    public static void runAt(@NotNull Plugin plugin, @NotNull Location location, @NotNull Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().execute(plugin, location, runnable);
        } else {
            if (Bukkit.isPrimaryThread()) {
                runnable.run();
            } else {
                Bukkit.getScheduler().runTask(plugin, runnable);
            }
        }
    }

    /**
     * Executes a runnable on the thread context owning a specific entity.
     */
    public static void runAt(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable runnable) {
        if (IS_FOLIA) {
            entity.getScheduler().run(plugin, task -> runnable.run(), null);
        } else {
            if (Bukkit.isPrimaryThread()) {
                runnable.run();
            } else {
                Bukkit.getScheduler().runTask(plugin, runnable);
            }
        }
    }

    /**
     * Asynchronously supplies a value computed on the region context of a location.
     * Returns a {@link CompletableFuture} that completes on the region thread.
     */
    @NotNull
    public static <T> CompletableFuture<T> supplyAt(@NotNull Plugin plugin, @NotNull Location location, @NotNull Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        runAt(plugin, location, () -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    /**
     * Asynchronously supplies a value computed on the context of an entity.
     */
    @NotNull
    public static <T> CompletableFuture<T> supplyAt(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        runAt(plugin, entity, () -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }
}
