# 🛠️ Paper Dev Toolkit

[![Java 21+](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![PaperMC](https://img.shields.io/badge/PaperMC-1.21.4-1A1A1A?style=for-the-badge&logo=minecraft&logoColor=white)](https://papermc.io/)
[![Folia](https://img.shields.io/badge/Folia-Native-3B82F6?style=for-the-badge)](https://github.com/PaperMC/Folia)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

A high-performance Java foundation library engineered for modern **PaperMC (1.21+)** and multi-threaded **Folia** server plugin architectures.

Built to solve the most painful limitations of modern Minecraft plugin development without NMS hacks, bytecode manipulation, or heavy third-party framework overhead. 100% safe to run alongside plugins like Towny, WorldGuard, EssentialsX, and LuckPerms.

---

## ⚡ Core Highlights

### 1. 🧩 Native 1.21.4 Data Components (`ItemComponentBuilder`)
Mojang completely removed NBT item tags in favor of structured **Data Components**. Paper Dev Toolkit provides a clean, fluent API to tap directly into modern vanilla mechanics:
- **Custom Max Stack Size:** Native 1-99 item stack sizing (stack potions to 16/64, unstackable gear) without packet tricks.
- **Edible Everything:** Transform *any* material (iron ingots, custom tokens, gems) into edible food with nutrition, saturation, and `canAlwaysEat`.
- **Wearable Any Slot:** Equip any item onto head, chest, legs, or feet with custom equip sounds and hotbar swapping.
- **Fire & Lava Immunity:** Native Netherite-tier fire resistance on custom items.
- **Enchantment Glint Override:** Force glint on non-enchanted items or suppress glint on enchanted items.

### 2. 🌐 Folia Cross-Region Safety (`RegionDispatcher`)
Folia's multi-threaded regional tick loop crashes traditional plugins with `IllegalStateException: Thread attempted to access entity outside of its region context`.
- **Automatic Thread-Hop:** Execute code on the exact region owning a target `Location` or `Entity`.
- **Cross-Region Promises:** Asynchronously request data across regional threads via `CompletableFuture` without deadlocks or server tick freezes.

### 3. ⏱️ Zero-Allocation Atomic Cooldowns (`FastCooldown`)
Avoid the garbage collection pauses caused by `Map<UUID, Long>` autoboxing in 300+ player PvP servers.
- Uses lock-free Compare-And-Set (`AtomicLong`) operations with microsecond-level precision.
- Single atomic check-and-acquire step (`tryAcquire`).

### 4. 🎨 Adventure MiniMessage & Solid Dividers (`Text`)
- First-class MiniMessage deserializer and color parser.
- Standardized, unbroken solid strikethrough dividers for GUIs (`<st><dark_gray>                        </dark_gray></st>`) and chat announcements.

### 5. 🪟 Reactive Inventory GUI Engine (`Gui`)
- Zero-boilerplate `InventoryHolder` menus with automated anti-theft click cancellations, border filling, and per-slot lambda listeners.

---

## 📦 Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.croxln:paper-dev-toolkit:1.0.0")
}
```

### Gradle (Groovy)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.croxln:paper-dev-toolkit:1.0.0'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.croxln</groupId>
        <artifactId>paper-dev-toolkit</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

---

## 🚀 Practical Examples

### 1. Create a Custom 1.21 Edible Token with Max Stack 64

```java
import io.github.croxln.toolkit.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

ItemStack manaCrystal = ItemBuilder.of(Material.AMETHYST_SHARD)
    .name("<gradient:#55FFFF:#00AAAA><b>Crystalline Mana Shard</b></gradient>")
    .divider()
    .addLore("<gray>Consume to instantly restore energy.")
    .divider()
    .maxStackSize(64)                  // Native 1.21 stack size
    .fireResistant(true)               // Netherite fire immunity
    .glintOverride(true)               // Enchantment shine without dummy enchants
    .food(food -> {                    // Native 1.21 food component
        food.nutrition(4);
        food.saturation(6.0f);
        food.canAlwaysEat(true);       // Can be eaten even when full
    })
    .build();
```

---

### 2. Folia Cross-Region Thread Dispatching

```java
import io.github.croxln.toolkit.folia.RegionDispatcher;
import org.bukkit.Location;
import org.bukkit.entity.Player;

// Safely execute an action inside the region owning a location
RegionDispatcher.runAt(plugin, targetLocation, () -> {
    targetLocation.getBlock().setType(Material.GOLD_BLOCK);
});

// Asynchronously query data from another region without crashing Folia
RegionDispatcher.supplyAt(plugin, otherPlayer, () -> otherPlayer.getHealth())
    .thenAccept(health -> {
        player.sendMessage("Target player health: " + health);
    });
```

---

### 3. Lock-Free Zero-Allocation Cooldowns

```java
import io.github.croxln.toolkit.cooldown.FastCooldown;

public final class SkillManager {
    private final FastCooldown combatCooldown = FastCooldown.create();

    public void handleCast(Player player) {
        // Atomically checks if allowed and applies 3000ms cooldown in one step
        if (!combatCooldown.tryAcquire(player.getUniqueId(), 3000L)) {
            long remainingSec = (combatCooldown.getRemainingMillis(player.getUniqueId()) / 1000) + 1;
            player.sendMessage("§cAbility on cooldown for " + remainingSec + "s!");
            return;
        }

        player.sendMessage("§aSkill unleashed!");
    }
}
```

---

### 4. Reactive Event-Driven Menu

```java
import io.github.croxln.toolkit.gui.Gui;
import io.github.croxln.toolkit.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

Gui warpMenu = Gui.of(3, "<dark_gray>Server Warps")
    .fillBorder(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
    .setItem(13, ItemBuilder.of(Material.NETHER_STAR).name("<gold><b>Main Hub</b></gold>").build(), event -> {
        Player p = (Player) event.getWhoClicked();
        p.sendMessage("Teleporting to hub...");
        p.closeInventory();
    });

warpMenu.open(player);
```

---

## 📄 License
Released under the [MIT License](LICENSE).