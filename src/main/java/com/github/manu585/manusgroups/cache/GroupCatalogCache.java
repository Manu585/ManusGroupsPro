package com.github.manu585.manusgroups.cache;

import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.repo.GroupRepository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Stores all created Groups in a no TTL cache for easy, fast, sync retrieval
 */
public final class GroupCatalogCache {
    private final ManualCache<String, Group> cache = new ManualCache<>();

    private final GroupRepository repository;

    public GroupCatalogCache(GroupRepository repository) {
        this.repository = repository;
    }

    /**
     * Load all groups from the DB into the cache
     */
    public CompletableFuture<Void> warmAll() {
        return repository.listGroups().thenAccept(groups -> {
            cache.clear();
            for (final Group group : groups) {
                cache.put(group.name(), group);
            }
        });
    }

    public void put(Group group) {
        cache.put(group.name(), group);
    }

    public Group get(String groupName) {
        return cache.get(groupName);
    }

    public void invalidate(String groupName) {
        cache.invalidate(groupName);
    }

    public void clear() {
        cache.clear();
    }

    public Map<String, Group> snapshot() {
        return cache.snapshot();
    }
}
