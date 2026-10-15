package io.github.croxln.toolkit;

import io.github.croxln.toolkit.gui.GuiListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main entry point for initializing Paper Dev Toolkit within a plugin.
 */
public final class Toolkit {

    private static JavaPlugin plugin;
    private static boolean initialized = false;

    private Toolkit() {}

    /**
     * Initializes the toolkit components, registering GUI listeners and hooks.
     *
     * @param hostingPlugin The JavaPlugin instance using this toolkit.
     */
    public static synchronized void init(@NotNull JavaPlugin hostingPlugin) {
        if (initialized) {
            return;
        }
        plugin = hostingPlugin;
        Bukkit.getPluginManager().registerEvents(new GuiListener(), hostingPlugin);
        initialized = true;
    }

    /**
     * Checks if the toolkit has been initialized.
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns the host plugin instance.
     */
    @NotNull
    public static JavaPlugin getPlugin() {
        if (!initialized || plugin == null) {
            throw new IllegalStateException("Paper Dev Toolkit has not been initialized! Call Toolkit.init(plugin) in your onEnable().");
        }
        return plugin;
    }
}
