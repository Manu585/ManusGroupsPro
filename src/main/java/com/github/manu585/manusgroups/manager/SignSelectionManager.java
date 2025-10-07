package com.github.manu585.manusgroups.manager;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SignSelectionManager {
    private final Map<UUID, Selection> selections = new ConcurrentHashMap<>();

    private static final long TTL_MILLIS = 60_000L;

    /**
     * Add an initiator to the selections Map
     * @param initiator Sign left clicker
     * @param target Sign target user
     */
    public void select(UUID initiator, UUID target) {
        selections.put(initiator, new Selection(target, Instant.now().toEpochMilli() + TTL_MILLIS));
    }

    /**
     * Consume the selection
     * @param initiator Sign left clicker
     * @return UUID of the Sign target user
     */
    public UUID consume(UUID initiator) {
        Selection selection = selections.remove(initiator);
        if (selection == null || selection.expired()) return null;

        return selection.target;
    }

    public void clear(UUID initiator) {
        selections.remove(initiator);
    }

    public void clearAll() {
        selections.clear();
    }

    private static final class Selection {
        final UUID target;
        final long expiresAtMs;

        Selection(UUID target, long expiresAtMs) {
            this.target = target;
            this.expiresAtMs = expiresAtMs;
        }

        boolean expired() {
            return Instant.now().toEpochMilli() >= expiresAtMs;
        }
    }
}
