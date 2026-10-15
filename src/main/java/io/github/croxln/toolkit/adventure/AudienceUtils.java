package io.github.croxln.toolkit.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Convenience methods for Adventure audience interactions (Action bars, Titles, Sound cues).
 */
public final class AudienceUtils {

    private AudienceUtils() {}

    /**
     * Sends an action bar to an audience.
     */
    public static void sendActionBar(@NotNull Audience audience, @NotNull Component message) {
        audience.sendActionBar(message);
    }

    /**
     * Sends an action bar using a MiniMessage format string.
     */
    public static void sendActionBar(@NotNull Audience audience, @NotNull String miniMessage) {
        audience.sendActionBar(Text.parse(miniMessage));
    }

    /**
     * Displays a full Adventure title with custom timing.
     */
    public static void sendTitle(
            @NotNull Audience audience,
            @NotNull Component title,
            @Nullable Component subtitle,
            long fadeInMillis,
            long stayMillis,
            long fadeOutMillis
    ) {
        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeInMillis),
                Duration.ofMillis(stayMillis),
                Duration.ofMillis(fadeOutMillis)
        );
        audience.showTitle(Title.title(title, subtitle != null ? subtitle : Component.empty(), times));
    }

    /**
     * Plays a pleasant UI click sound to a player.
     */
    public static void playClickSound(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
    }

    /**
     * Plays a standard success cue sound.
     */
    public static void playSuccessSound(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.4f);
    }

    /**
     * Plays an error notification sound.
     */
    public static void playErrorSound(@NotNull Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.8f, 1.0f);
    }
}
