package com.github.manu585.manusgroups.cache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ManualCache<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();

    public V get(K key) {
        return cache.get(key);
    }

    public void put(K key, V value) {
        if (value == null) {
            invalidate(key);
        } else {
            cache.put(key, value);
        }
    }

    public void invalidate(K key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    public @NotNull @UnmodifiableView Map<K, V> snapshot() {
        return Collections.unmodifiableMap(cache);
    }
}
