package com.github.manu585.manusgroups.cache;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public final class PrefixCache {
    private final ManualCache<UUID, Component> cache = new ManualCache<>();

    public Component getIfPresent(final UUID user) {
        return cache.get(user);
    }

    public Component getOrDefault(final UUID user) {
        final Component component = cache.get(user);
        return (component == null) ? Component.empty() : component;
    }

    public void put(final UUID user, final Component component) {
        cache.put(user, component);
    }

    public void invalidate(final UUID user) {
        cache.invalidate(user);
    }

    public void clear() {
        cache.clear();
    }
}
