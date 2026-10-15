package io.github.croxln.toolkit.cooldown;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance, scoped, and reversible cooldown engine.
 * Extracted from production server architecture (SrRTP).
 * Supports token-based rollback, multi-scope checking, usage locking, and persistence snapshots.
 */
public final class CooldownService {

    public static final String GLOBAL = "global";

    private record Key(UUID player, String scope) {}

    private final InstantSource clock;
    private final ConcurrentHashMap<Key, Instant> until = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Integer> uses = new ConcurrentHashMap<>();

    public CooldownService(@NotNull InstantSource clock) {
        this.clock = clock;
    }

    public CooldownService() {
        this(InstantSource.system());
    }

    /**
     * Handle returned by {@link #start}; {@link #revert()} restores the exact previous state.
     */
    public final class Token {
        private final Key key;
        private final Instant previous;
        private boolean reverted;

        private Token(Key key, Instant previous) {
            this.key = key;
            this.previous = previous;
        }

        /**
         * Undoes the cooldown (e.g. action cancelled, warp failed, transaction rolled back).
         * Idempotent and thread-safe.
         */
        public synchronized void revert() {
            if (reverted) return;
            reverted = true;
            if (previous == null) {
                until.remove(key);
            } else {
                until.put(key, previous);
            }
            uses.computeIfPresent(key.player(), (u, n) -> Math.max(0, n - 1));
        }
    }

    /**
     * Starts a cooldown for a player under a specific scope and counts one usage.
     */
    @NotNull
    public Token start(@NotNull UUID player, @NotNull String scope, @NotNull Duration duration) {
        Key key = new Key(player, scope);
        Instant previous = until.get(key);
        if (!duration.isZero() && !duration.isNegative()) {
            until.put(key, clock.instant().plus(duration));
        }
        uses.merge(player, 1, Integer::sum);
        return new Token(key, previous);
    }

    /**
     * Starts a global scope cooldown for a player.
     */
    @NotNull
    public Token start(@NotNull UUID player, @NotNull Duration duration) {
        return start(player, GLOBAL, duration);
    }

    /**
     * Returns remaining duration for a player under a specific scope, or Duration.ZERO if expired.
     */
    @NotNull
    public Duration remaining(@NotNull UUID player, @NotNull String scope) {
        Key key = new Key(player, scope);
        Instant end = until.get(key);
        if (end == null) return Duration.ZERO;
        Duration left = Duration.between(clock.instant(), end);
        if (left.isNegative() || left.isZero()) {
            until.remove(key, end);
            return Duration.ZERO;
        }
        return left;
    }

    /**
     * Returns remaining duration under global scope.
     */
    @NotNull
    public Duration remaining(@NotNull UUID player) {
        return remaining(player, GLOBAL);
    }

    /**
     * Finds the longest remaining cooldown duration across several scopes.
     */
    @NotNull
    public Duration remainingAny(@NotNull UUID player, @NotNull String... scopes) {
        Duration best = Duration.ZERO;
        for (String s : scopes) {
            Duration d = remaining(player, s);
            if (d.compareTo(best) > 0) best = d;
        }
        return best;
    }

    /**
     * Checks if the player is currently on cooldown under a specific scope.
     */
    public boolean isCoolingDown(@NotNull UUID player, @NotNull String scope) {
        return !remaining(player, scope).isZero();
    }

    /**
     * Checks if the player is currently on global cooldown.
     */
    public boolean isCoolingDown(@NotNull UUID player) {
        return isCoolingDown(player, GLOBAL);
    }

    /**
     * Returns the total recorded uses for a player.
     */
    public int useCount(@NotNull UUID player) {
        return uses.getOrDefault(player, 0);
    }

    /**
     * Checks if the player has reached or exceeded max uses threshold ({@code lockAfter <= 0} disables locking).
     */
    public boolean isLocked(@NotNull UUID player, int lockAfter) {
        return lockAfter > 0 && useCount(player) >= lockAfter;
    }

    /**
     * Clears all cooldowns and use counts for a player.
     */
    public void clear(@NotNull UUID player) {
        until.keySet().removeIf(k -> k.player().equals(player));
        uses.remove(player);
    }

    /**
     * Exports an active memory snapshot for storage (player -> scope -> expiry epoch millis).
     * Expired entries are automatically dropped.
     */
    @NotNull
    public Map<UUID, Map<String, Long>> snapshot() {
        Instant now = clock.instant();
        Map<UUID, Map<String, Long>> out = new HashMap<>();
        until.forEach((k, end) -> {
            if (end.isAfter(now)) {
                out.computeIfAbsent(k.player(), p -> new HashMap<>()).put(k.scope(), end.toEpochMilli());
            }
        });
        return out;
    }

    /**
     * Restores a persistent cooldown from database/config storage.
     */
    public void restore(@NotNull UUID player, @NotNull String scope, long untilEpochMillis) {
        Instant end = Instant.ofEpochMilli(untilEpochMillis);
        if (end.isAfter(clock.instant())) {
            until.put(new Key(player, scope), end);
        }
    }

    /**
     * Restores player use count from storage.
     */
    public void restoreUseCount(@NotNull UUID player, int count) {
        if (count > 0) {
            uses.put(player, count);
        }
    }
}
