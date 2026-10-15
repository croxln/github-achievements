package io.github.croxln.toolkit.gui;

import io.github.croxln.toolkit.adventure.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Modern, lightweight reactive GUI abstraction.
 */
public class Gui implements InventoryHolder {

    private final int rows;
    private final int size;
    private final Component title;
    private final Inventory inventory;
    private final Map<Integer, GuiItem> items = new HashMap<>();

    private boolean cancelClicks = true;
    private Consumer<InventoryCloseEvent> closeHandler;
    private Consumer<InventoryOpenEvent> openHandler;

    public Gui(int rows, @NotNull Component title) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6, received: " + rows);
        }
        this.rows = rows;
        this.size = rows * 9;
        this.title = title;
        this.inventory = Bukkit.createInventory(this, this.size, title);
    }

    public Gui(int rows, @NotNull String miniMessageTitle) {
        this(rows, Text.parse(miniMessageTitle));
    }

    @NotNull
    public static Gui of(int rows, @NotNull Component title) {
        return new Gui(rows, title);
    }

    @NotNull
    public static Gui of(int rows, @NotNull String miniMessageTitle) {
        return new Gui(rows, miniMessageTitle);
    }

    public int getRows() {
        return rows;
    }

    public int getSize() {
        return size;
    }

    public boolean isCancelClicks() {
        return cancelClicks;
    }

    public Gui setCancelClicks(boolean cancelClicks) {
        this.cancelClicks = cancelClicks;
        return this;
    }

    public Gui onClose(@Nullable Consumer<InventoryCloseEvent> closeHandler) {
        this.closeHandler = closeHandler;
        return this;
    }

    public Gui onOpen(@Nullable Consumer<InventoryOpenEvent> openHandler) {
        this.openHandler = openHandler;
        return this;
    }

    public Gui setItem(int slot, @Nullable GuiItem guiItem) {
        if (slot < 0 || slot >= size) return this;
        if (guiItem == null) {
            items.remove(slot);
            inventory.setItem(slot, null);
        } else {
            items.put(slot, guiItem);
            inventory.setItem(slot, guiItem.getItem());
        }
        return this;
    }

    public Gui setItem(int slot, @NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> onClick) {
        return setItem(slot, new GuiItem(item, onClick));
    }

    public Gui setItem(int slot, @NotNull ItemStack item) {
        return setItem(slot, item, null);
    }

    public Gui setItem(int row, int col, @NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> onClick) {
        int slot = (row * 9) + col;
        return setItem(slot, item, onClick);
    }

    public Gui fill(@NotNull ItemStack item) {
        for (int i = 0; i < size; i++) {
            if (!items.containsKey(i)) {
                setItem(i, item.clone(), null);
            }
        }
        return this;
    }

    public Gui fillBorder(@NotNull ItemStack item) {
        for (int col = 0; col < 9; col++) {
            setItem(0, col, item.clone(), null);
            if (rows > 1) {
                setItem(rows - 1, col, item.clone(), null);
            }
        }
        for (int row = 1; row < rows - 1; row++) {
            setItem(row, 0, item.clone(), null);
            setItem(row, 8, item.clone(), null);
        }
        return this;
    }

    public Gui fillRow(int row, @NotNull ItemStack item) {
        if (row < 0 || row >= rows) return this;
        for (int col = 0; col < 9; col++) {
            setItem(row, col, item.clone(), null);
        }
        return this;
    }

    public void open(@NotNull Player player) {
        player.openInventory(this.inventory);
    }

    public void handleOpen(@NotNull InventoryOpenEvent event) {
        if (openHandler != null) {
            openHandler.accept(event);
        }
    }

    public void handleClose(@NotNull InventoryCloseEvent event) {
        if (closeHandler != null) {
            closeHandler.accept(event);
        }
    }

    public void handleClick(int slot, @NotNull InventoryClickEvent event) {
        GuiItem guiItem = items.get(slot);
        if (guiItem != null) {
            guiItem.handleClick(event);
        }
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}
