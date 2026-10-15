package io.github.croxln.toolkit.pdc;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Type-safe helper for Bukkit/Paper PersistentDataContainer.
 */
public final class PdcHelper {

    private PdcHelper() {}

    /**
     * Sets a string value.
     */
    public static void setString(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull String value) {
        holder.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
    }

    /**
     * Gets a string value or null if not present.
     */
    @Nullable
    public static String getString(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        return holder.getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    /**
     * Sets an integer value.
     */
    public static void setInt(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, int value) {
        holder.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, value);
    }

    /**
     * Gets an integer value or default if not present.
     */
    public static int getInt(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, int defaultValue) {
        Integer val = holder.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return val != null ? val : defaultValue;
    }

    /**
     * Sets a long value.
     */
    public static void setLong(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, long value) {
        holder.getPersistentDataContainer().set(key, PersistentDataType.LONG, value);
    }

    /**
     * Gets a long value or default if not present.
     */
    public static long getLong(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, long defaultValue) {
        Long val = holder.getPersistentDataContainer().get(key, PersistentDataType.LONG);
        return val != null ? val : defaultValue;
    }

    /**
     * Sets a double value.
     */
    public static void setDouble(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, double value) {
        holder.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, value);
    }

    /**
     * Gets a double value or default if not present.
     */
    public static double getDouble(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, double defaultValue) {
        Double val = holder.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
        return val != null ? val : defaultValue;
    }

    /**
     * Sets a boolean value (persisted as byte 1 or 0).
     */
    public static void setBoolean(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, boolean value) {
        holder.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
    }

    /**
     * Gets a boolean value or default if not present.
     */
    public static boolean getBoolean(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, boolean defaultValue) {
        Byte val = holder.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return val != null ? (val == 1) : defaultValue;
    }

    /**
     * Sets a UUID value (persisted as standard string).
     */
    public static void setUuid(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key, @NotNull UUID value) {
        setString(holder, key, value.toString());
    }

    /**
     * Gets a UUID value or null if not present.
     */
    @Nullable
    public static UUID getUuid(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        String val = getString(holder, key);
        if (val == null) return null;
        try {
            return UUID.fromString(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Checks if a key exists in the persistent container.
     */
    public static boolean has(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        return holder.getPersistentDataContainer().has(key);
    }

    /**
     * Removes a key from the persistent container.
     */
    public static void remove(@NotNull PersistentDataHolder holder, @NotNull NamespacedKey key) {
        holder.getPersistentDataContainer().remove(key);
    }

    /* Convenience helpers for ItemStack */

    public static void setString(@NotNull ItemStack item, @NotNull NamespacedKey key, @NotNull String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            setString(meta, key, value);
            item.setItemMeta(meta);
        }
    }

    @Nullable
    public static String getString(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        ItemMeta meta = item.getItemMeta();
        return (meta != null) ? getString(meta, key) : null;
    }

    public static void setBoolean(@NotNull ItemStack item, @NotNull NamespacedKey key, boolean value) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            setBoolean(meta, key, value);
            item.setItemMeta(meta);
        }
    }

    public static boolean getBoolean(@NotNull ItemStack item, @NotNull NamespacedKey key, boolean defaultValue) {
        ItemMeta meta = item.getItemMeta();
        return (meta != null) ? getBoolean(meta, key, defaultValue) : defaultValue;
    }
}
