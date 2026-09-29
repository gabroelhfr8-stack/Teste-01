package com.seleris.selarium.ward.effect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-effect, per-entity cooldown table that prunes itself. Server thread only. */
public final class EntityCooldowns {
    private static final long PRUNE_GRACE_TICKS = 20L;
    private final Map<UUID, Long> readyAt = new HashMap<>();

    public boolean ready(UUID id, long now) {
        return readyAt.getOrDefault(id, 0L) <= now;
    }

    public void start(UUID id, long readyAtTick) {
        readyAt.put(id, readyAtTick);
    }

    public void prune(long now) {
        readyAt.entrySet().removeIf(entry -> entry.getValue() + PRUNE_GRACE_TICKS < now);
    }
}
