package io.github.croxln.toolkit.component;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.inventory.meta.components.ToolComponent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * High-level, fluent builder for Minecraft 1.20.5+ / 1.21.4 native Data Components.
 * Replaces legacy NBT with first-class vanilla item mechanics.
 */
public final class ItemComponentBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    private ItemComponentBuilder(@NotNull ItemStack item) {
        this.item = item.clone();
        ItemMeta m = this.item.getItemMeta();
        this.meta = (m != null) ? m : org.bukkit.Bukkit.getItemFactory().getItemMeta(this.item.getType());
    }

    @NotNull
    public static ItemComponentBuilder of(@NotNull Material material) {
        return new ItemComponentBuilder(new ItemStack(material));
    }

    @NotNull
    public static ItemComponentBuilder from(@NotNull ItemStack stack) {
        return new ItemComponentBuilder(stack);
    }

    /**
     * Overrides the maximum stack size of this item (1 to 99).
     * Allows native stackable potions, pearls, or unstackable custom gear.
     */
    @NotNull
    public ItemComponentBuilder maxStackSize(int size) {
        if (size < 1 || size > 99) {
            throw new IllegalArgumentException("Stack size must be between 1 and 99, received: " + size);
        }
        if (meta != null) {
            meta.setMaxStackSize(size);
        }
        return this;
    }

    /**
     * Makes this item completely immune to fire and lava damage (Netherite tier behavior).
     */
    @NotNull
    public ItemComponentBuilder fireResistant(boolean fireResistant) {
        if (meta != null) {
            meta.setFireResistant(fireResistant);
        }
        return this;
    }

    /**
     * Hides the entire vanilla tooltip of this item in the inventory.
     */
    @NotNull
    public ItemComponentBuilder hideTooltip(boolean hideTooltip) {
        if (meta != null) {
            meta.setHideTooltip(hideTooltip);
        }
        return this;
    }

    /**
     * Controls the enchantment glint appearance without requiring dummy enchantments.
     * True forces glint; false removes glint even if enchanted; null restores vanilla default.
     */
    @NotNull
    public ItemComponentBuilder glintOverride(@Nullable Boolean glint) {
        if (meta != null) {
            meta.setEnchantmentGlintOverride(glint);
        }
        return this;
    }

    /**
     * Configures modern 1.21+ food properties, allowing ANY item to be edible.
     */
    @NotNull
    public ItemComponentBuilder food(@NotNull Consumer<FoodModifier> consumer) {
        if (meta != null) {
            FoodComponent foodComponent = meta.getFood();
            FoodModifier modifier = new FoodModifier(foodComponent);
            consumer.accept(modifier);
            meta.setFood(foodComponent);
        }
        return this;
    }

    /**
     * Configures modern 1.21+ equippable properties, allowing ANY item to be wearable.
     */
    @NotNull
    public ItemComponentBuilder equippable(@NotNull Consumer<EquippableModifier> consumer) {
        if (meta != null) {
            EquippableComponent equippableComponent = meta.getEquippable();
            EquippableModifier modifier = new EquippableModifier(equippableComponent);
            consumer.accept(modifier);
            meta.setEquippable(equippableComponent);
        }
        return this;
    }

    /**
     * Configures modern 1.21+ tool rules and custom mining speed mechanics.
     */
    @NotNull
    public ItemComponentBuilder tool(@NotNull Consumer<ToolModifier> consumer) {
        if (meta != null) {
            ToolComponent toolComponent = meta.getTool();
            ToolModifier modifier = new ToolModifier(toolComponent);
            consumer.accept(modifier);
            meta.setTool(toolComponent);
        }
        return this;
    }

    @NotNull
    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Fluent modifier for 1.21+ {@link FoodComponent}.
     */
    public static final class FoodModifier {
        private final FoodComponent food;

        public FoodModifier(FoodComponent food) {
            this.food = food;
        }

        public FoodModifier nutrition(int nutrition) {
            food.setNutrition(Math.max(0, nutrition));
            return this;
        }

        public FoodModifier saturation(float saturation) {
            food.setSaturation(Math.max(0.0f, saturation));
            return this;
        }

        public FoodModifier canAlwaysEat(boolean canAlwaysEat) {
            food.setCanAlwaysEat(canAlwaysEat);
            return this;
        }
    }

    /**
     * Fluent modifier for 1.21+ {@link EquippableComponent}.
     */
    public static final class EquippableModifier {
        private final EquippableComponent equippable;

        public EquippableModifier(EquippableComponent equippable) {
            this.equippable = equippable;
        }

        public EquippableModifier slot(@NotNull EquipmentSlot slot) {
            equippable.setSlot(Objects.requireNonNull(slot, "Slot cannot be null"));
            return this;
        }

        public EquippableModifier equipSound(@Nullable Sound sound) {
            equippable.setEquipSound(sound);
            return this;
        }

        public EquippableModifier swappable(boolean swappable) {
            equippable.setSwappable(swappable);
            return this;
        }

        public EquippableModifier damageOnHurt(boolean damageOnHurt) {
            equippable.setDamageOnHurt(damageOnHurt);
            return this;
        }

        public EquippableModifier dispensable(boolean dispensable) {
            equippable.setDispensable(dispensable);
            return this;
        }
    }

    /**
     * Fluent modifier for 1.21+ {@link ToolComponent}.
     */
    public static final class ToolModifier {
        private final ToolComponent tool;

        public ToolModifier(ToolComponent tool) {
            this.tool = tool;
        }

        public ToolModifier defaultMiningSpeed(float speed) {
            tool.setDefaultMiningSpeed(Math.max(0.0f, speed));
            return this;
        }

        public ToolModifier damagePerBlock(int damage) {
            tool.setDamagePerBlock(Math.max(0, damage));
            return this;
        }
    }
}
