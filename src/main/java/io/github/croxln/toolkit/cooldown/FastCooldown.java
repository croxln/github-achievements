package io.github.croxln.toolkit.cooldown;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Ultra-fast, zero-allocation rate limiter and cooldown manager.
 * Designed for high-frequency PvP actions, skills, and command spam protection.
 */
public final class FastCooldown {

    private final ConcurrentHashMap<UUID, AtomicLong> expiryMap = new ConcurrentHashMap<>();

    private FastCooldown() {}

    @NotNull
    public static FastCooldown create() {
        return new FastCooldown();
    }

    /**
     * Atomically tests if the action is allowed, and if so, applies the cooldown.
     *
     * @param id             The target UUID (e.g. player UUID).
     * @param cooldownMillis The cooldown duration in milliseconds.
     * @return {@code true} if the cooldown was not active and has now been acquired; {@code false} if still active.
     */
    public boolean tryAcquire(@NotNull UUID id, long cooldownMillis) {
        long now = System.currentTimeMillis();
        long targetExpiry = now + cooldownMillis;

        AtomicLong cell = expiryMap.computeIfAbsent(id, k -> new AtomicLong(0L));
        while (true) {
            long currentExpiry = cell.get();
            if (now < currentExpiry) {
                return false;
            }
            if (cell.compareAndSet(currentExpiry, targetExpiry)) {
                return true;
            }
        }
    }

    /**
     * Returns the remaining cooldown duration in milliseconds, or 0 if expired.
     */
    public long getRemainingMillis(@NotNull UUID id) {
        AtomicLong cell = expiryMap.get(id);
        if (cell == null) {
            return 0L;
        }
        long remaining = cell.get() - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    /**
     * Checks if the entity is currently on cooldown without modifying the state.
     */
    public boolean isOnCooldown(@NotNull UUID id) {
        return getRemainingMillis(id) > 0L;
    }

    /**
     * Resets/clears the cooldown for a specific UUID.
     */
    public void reset(@NotNull UUID id) {
        AtomicLong cell = expiryMap.get(id);
        if (cell != null) {
            cell.set(0L);
        }
    }

    /**
     * Purges expired entries from memory to prevent long-term memory leaks.
     */
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        expiryMap.entrySet().removeIf(entry -> entry.getValue().get() <= now);
    }

    /**
     * Clears all cooldowns.
     */
    public void clear() {
        expiryMap.clear();
    }
}
