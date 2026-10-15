# 🛠️ Paper Dev Toolkit

[![Java 21+](https://img.shields.io/badge/Java-21%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![PaperMC](https://img.shields.io/badge/PaperMC-1.21.4-1A1A1A?style=for-the-badge&logo=minecraft&logoColor=white)](https://papermc.io/)
[![Folia](https://img.shields.io/badge/Folia-Supported-3B82F6?style=for-the-badge)](https://github.com/PaperMC/Folia)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

A modular, lightweight Java foundation library designed for modern **PaperMC (1.21+)** and **Folia** server plugin development. 

Zero bloated dependencies, zero unnecessary overhead. Pure Java and native Paper/Adventure APIs.

---

## ✨ Features

- 📜 **Universal Scheduler**: Run tasks synchronously, asynchronously, or bound to regions/entities seamlessly across both traditional **Paper** and multi-threaded **Folia** engines without code branches.
- 🎨 **Adventure & MiniMessage**: First-class MiniMessage text parsing, tag resolvers, gradient helpers, and standardized smooth GUI dividers.
- 🪟 **Reactive GUIs**: Event-driven `InventoryHolder` menus with zero boilerplate, automated anti-theft click cancellations, and per-slot lambdas.
- 🗡️ **Fluent ItemBuilder**: Chainable item construction with MiniMessage names, lore formatting, persistent data tags, and custom model data.
- 💾 **Type-Safe PDC**: Fast, strongly-typed wrappers around Bukkit's `PersistentDataContainer` (Strings, Integers, Doubles, Booleans, and UUIDs).

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

## 🚀 Quick Start

### 1. Initialize Toolkit in your Plugin
In your `onEnable()`, initialize the toolkit once to register GUI listeners:

```java
import io.github.croxln.toolkit.Toolkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        Toolkit.init(this);
    }
}
```

---

### 2. Universal Folia & Paper Scheduler
Run tasks anywhere without checking whether your server runs on Paper or Folia:

```java
import io.github.croxln.toolkit.scheduler.UniversalScheduler;

// Run sync or async
UniversalScheduler.runSync(plugin, () -> {
    player.sendMessage("Executed on the main/global thread");
});

UniversalScheduler.runAsync(plugin, () -> {
    // Database or network query
});

// Run bound to an entity or region (vital for Folia)
UniversalScheduler.runAtEntity(plugin, player, () -> {
    player.teleport(targetLocation);
});
```

---

### 3. Fluent ItemBuilder & MiniMessage
Build complex items with smooth dividers and persistent data in seconds:

```java
import io.github.croxln.toolkit.builder.ItemBuilder;
import io.github.croxln.toolkit.adventure.Text;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

ItemStack rewardSword = ItemBuilder.of(Material.NETHERITE_SWORD)
    .name("<gradient:#FF5555:#FFAA00><b>Legendary Blade</b></gradient>")
    .divider()
    .addLore("<gray>Forged in the depths of the Nether.")
    .addLore("<yellow>Right-Click to unleash flames!")
    .divider()
    .customModelData(1001)
    .glow(true)
    .unbreakable(true)
    .build();
```

---

### 4. Reactive GUIs
Create fully functional inventory menus with click handlers and automatic protection against item theft:

```java
import io.github.croxln.toolkit.gui.Gui;
import io.github.croxln.toolkit.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

Gui menu = Gui.of(3, "<dark_gray>Server Warp Menu")
    .fillBorder(ItemBuilder.of(Material.GRAY_STAINED_GLASS_PANE).name(" ").build())
    .setItem(13, ItemBuilder.of(Material.COMPASS).name("<green>Spawn").build(), event -> {
        Player player = (Player) event.getWhoClicked();
        player.sendMessage(Text.parse("<green>Teleporting to spawn..."));
        player.closeInventory();
    });

menu.open(player);
```

---

### 5. Type-Safe Persistent Data (PDC)
Attach and retrieve data on items, entities, or blocks cleanly:

```java
import io.github.croxln.toolkit.pdc.PdcHelper;
import org.bukkit.NamespacedKey;

NamespacedKey KEY_OWNER = new NamespacedKey(plugin, "owner_id");

// Store data
PdcHelper.setUuid(itemStack, KEY_OWNER, player.getUniqueId());

// Retrieve data safely
UUID ownerId = PdcHelper.getUuid(itemStack, KEY_OWNER);
```

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).