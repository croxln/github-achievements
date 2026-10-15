package io.github.croxln.toolkit.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * High-performance Adventure and MiniMessage utility for modern PaperMC plugins.
 */
public final class Text {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer AMP_LEGACY = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .build();

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    /**
     * Standard unbroken smooth divider for GUIs and item lore.
     * Guaranteed solid 24-space strikethrough.
     */
    public static final String DIVIDER_STRING = "<st><dark_gray>                        </dark_gray></st>";
    public static final Component DIVIDER = parse(DIVIDER_STRING);

    /**
     * Standard unbroken smooth divider for chat announcements.
     */
    public static final String CHAT_DIVIDER_STRING = "<dark_gray><st>                                             </st></dark_gray>";
    public static final Component CHAT_DIVIDER = parse(CHAT_DIVIDER_STRING);

    private Text() {}

    /**
     * Parses a MiniMessage formatted string into an Adventure {@link Component}.
     * Returns {@link Component#empty()} if input is null or blank.
     */
    @NotNull
    public static Component parse(@Nullable String miniMessage) {
        if (miniMessage == null || miniMessage.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(miniMessage);
    }

    /**
     * Parses a MiniMessage formatted string with dynamic tag resolvers.
     */
    @NotNull
    public static Component parse(@Nullable String miniMessage, @NotNull TagResolver... resolvers) {
        if (miniMessage == null || miniMessage.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(miniMessage, resolvers);
    }

    /**
     * Parses a list of MiniMessage strings into a list of Components.
     */
    @NotNull
    public static List<Component> parseList(@Nullable List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return new ArrayList<>();
        }
        List<Component> components = new ArrayList<>(lines.size());
        for (String line : lines) {
            components.add(parse(line));
        }
        return components;
    }

    /**
     * Converts a string with legacy ampersand color codes ({@code &a}, {@code &l}, {@code &#RRGGBB})
     * into a MiniMessage compliant Component.
     */
    @NotNull
    public static Component fromLegacy(@Nullable String legacy) {
        if (legacy == null || legacy.isEmpty()) {
            return Component.empty();
        }
        return AMP_LEGACY.deserialize(legacy);
    }

    /**
     * Serializes a Component back into a MiniMessage string.
     */
    @NotNull
    public static String serialize(@NotNull Component component) {
        return MINI_MESSAGE.serialize(component);
    }

    /**
     * Strips all formatting and returns raw plain text.
     */
    @NotNull
    public static String toPlainText(@NotNull Component component) {
        return PLAIN.serialize(component);
    }

    /**
     * Creates a smooth gradient component between two text colors.
     */
    @NotNull
    public static Component gradient(@NotNull String text, @NotNull TextColor from, @NotNull TextColor to) {
        return parse("<gradient:" + from.asHexString() + ":" + to.asHexString() + ">" + text + "</gradient>");
    }
}
