package io.github.croxln.toolkit.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * An item inside a {@link Gui} accompanied by an optional click callback.
 */
public final class GuiItem {

    private final ItemStack item;
    private final Consumer<InventoryClickEvent> clickHandler;

    public GuiItem(@NotNull ItemStack item, @Nullable Consumer<InventoryClickEvent> clickHandler) {
        this.item = item;
        this.clickHandler = clickHandler;
    }

    public GuiItem(@NotNull ItemStack item) {
        this(item, null);
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @Nullable
    public Consumer<InventoryClickEvent> getClickHandler() {
        return clickHandler;
    }

    public void handleClick(@NotNull InventoryClickEvent event) {
        if (clickHandler != null) {
            clickHandler.accept(event);
        }
    }
}
