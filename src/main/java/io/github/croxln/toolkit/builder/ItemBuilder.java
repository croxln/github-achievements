package io.github.croxln.toolkit.builder;

import io.github.croxln.toolkit.adventure.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Modern, fluent Item builder for Adventure and PaperMC 1.21+.
 */
public final class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;
    private final List<Component> lore = new ArrayList<>();

    private ItemBuilder(@NotNull ItemStack item) {
        this.item = item.clone();
        ItemMeta m = this.item.getItemMeta();
        this.meta = (m != null) ? m : org.bukkit.Bukkit.getItemFactory().getItemMeta(item.getType());
        if (this.meta != null && this.meta.hasLore()) {
            List<Component> existing = this.meta.lore();
            if (existing != null) {
                this.lore.addAll(existing);
            }
        }
    }

    @NotNull
    public static ItemBuilder of(@NotNull Material material) {
        return new ItemBuilder(new ItemStack(material, 1));
    }

    @NotNull
    public static ItemBuilder of(@NotNull Material material, int amount) {
        return new ItemBuilder(new ItemStack(material, Math.max(1, amount)));
    }

    @NotNull
    public static ItemBuilder from(@NotNull ItemStack stack) {
        return new ItemBuilder(stack);
    }

    @NotNull
    public ItemBuilder amount(int amount) {
        this.item.setAmount(Math.max(1, amount));
        return this;
    }

    @NotNull
    public ItemBuilder name(@NotNull Component component) {
        if (this.meta != null) {
            this.meta.displayName(component);
        }
        return this;
    }

    @NotNull
    public ItemBuilder name(@NotNull String miniMessage) {
        return name(Text.parse(miniMessage));
    }

    @NotNull
    public ItemBuilder lore(@NotNull List<Component> components) {
        this.lore.clear();
        this.lore.addAll(components);
        return this;
    }

    @NotNull
    public ItemBuilder lore(@NotNull Component... components) {
        this.lore.clear();
        this.lore.addAll(Arrays.asList(components));
        return this;
    }

    @NotNull
    public ItemBuilder lore(@NotNull String... miniMessageLines) {
        this.lore.clear();
        for (String line : miniMessageLines) {
            this.lore.add(Text.parse(line));
        }
        return this;
    }

    @NotNull
    public ItemBuilder addLore(@NotNull Component... components) {
        this.lore.addAll(Arrays.asList(components));
        return this;
    }

    @NotNull
    public ItemBuilder addLore(@NotNull String... miniMessageLines) {
        for (String line : miniMessageLines) {
            this.lore.add(Text.parse(line));
        }
        return this;
    }

    /**
     * Appends a solid, unbroken 24-space strikethrough divider to the lore.
     */
    @NotNull
    public ItemBuilder divider() {
        this.lore.add(Text.DIVIDER);
        return this;
    }

    @NotNull
    public ItemBuilder customModelData(@Nullable Integer data) {
        if (this.meta != null) {
            this.meta.setCustomModelData(data);
        }
        return this;
    }

    @NotNull
    public ItemBuilder enchant(@NotNull Enchantment enchantment, int level) {
        return enchant(enchantment, level, true);
    }

    @NotNull
    public ItemBuilder enchant(@NotNull Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        if (this.meta != null) {
            this.meta.addEnchant(enchantment, level, ignoreLevelRestriction);
        }
        return this;
    }

    @NotNull
    public ItemBuilder flags(@NotNull ItemFlag... flags) {
        if (this.meta != null) {
            this.meta.addItemFlags(flags);
        }
        return this;
    }

    @NotNull
    public ItemBuilder hideAll() {
        if (this.meta != null) {
            this.meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    @NotNull
    public ItemBuilder unbreakable(boolean unbreakable) {
        if (this.meta != null) {
            this.meta.setUnbreakable(unbreakable);
        }
        return this;
    }

    @NotNull
    public ItemBuilder glow(boolean glow) {
        if (glow) {
            flags(ItemFlag.HIDE_ENCHANTS);
            enchant(Enchantment.UNBREAKING, 1);
        }
        return this;
    }

    @NotNull
    public ItemBuilder color(@NotNull Color color) {
        if (this.meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
        }
        return this;
    }

    @NotNull
    public ItemBuilder skullOwner(@NotNull OfflinePlayer player) {
        if (this.meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    @NotNull
    public <T, Z> ItemBuilder pdc(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z value) {
        if (this.meta != null) {
            this.meta.getPersistentDataContainer().set(key, type, value);
        }
        return this;
    }

    @NotNull
    public ItemBuilder applyMeta(@NotNull Consumer<ItemMeta> consumer) {
        if (this.meta != null) {
            consumer.accept(this.meta);
        }
        return this;
    }

    @NotNull
    public ItemStack build() {
        if (this.meta != null) {
            this.meta.lore(this.lore.isEmpty() ? null : this.lore);
            this.item.setItemMeta(this.meta);
        }
        return this.item;
    }
}
