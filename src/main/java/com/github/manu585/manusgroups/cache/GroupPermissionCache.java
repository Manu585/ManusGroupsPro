package com.github.manu585.manusgroups.cache;

import com.github.manu585.manusgroups.repo.GroupRepository;
import com.github.manu585.manusgroups.util.General;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class GroupPermissionCache {
    private final ManualCache<String, Map<String, Boolean>> cache = new ManualCache<>();

    private final GroupRepository repository;

    public GroupPermissionCache(final GroupRepository repository) {
        this.repository = repository;
    }

    /**
     * @return Cached raw permissions for a group
     */
    public CompletableFuture<Map<String, Boolean>> getOrLoad(final String groupName) {
        final Map<String, Boolean> cached = cache.get(groupName);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        // Load raw map from DB
        return repository.listPermissionsByGroup(groupName).thenApply(raw -> {
            final Map<String, Boolean> immutable = Map.copyOf(raw);
            cache.put(groupName, immutable);
            return immutable;
        });
    }

    public CompletableFuture<Void> warmAll(Iterable<String> groupNames) {
        final List<CompletableFuture<?>> tasks = new ArrayList<>();
        for (String group : groupNames) {
            tasks.add(getOrLoad(group));
        }

        return General.allDone(tasks);
    }

    public Map<String, Boolean> getIfPresent(final String groupName) {
        return cache.get(groupName);
    }

    public void invalidate(final String groupName) {
        cache.invalidate(groupName);
    }

    public void clear() {
        cache.clear();
    }
}
