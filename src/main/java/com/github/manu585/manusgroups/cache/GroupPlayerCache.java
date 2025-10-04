package com.github.manu585.manusgroups.cache;

import com.github.manu585.manusgroups.defaults.DefaultGroup;
import com.github.manu585.manusgroups.domain.GroupAssignment;
import com.github.manu585.manusgroups.domain.GroupPlayer;
import com.github.manu585.manusgroups.repo.GroupRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class GroupPlayerCache {
    private final ManualCache<UUID, GroupPlayer> cache = new ManualCache<>();

    private final GroupRepository repository;
    private final GroupCatalogCache catalogCache;

    public GroupPlayerCache(final GroupRepository repository, final GroupCatalogCache catalogCache) {
        this.repository = repository;
        this.catalogCache = catalogCache;
    }

    public CompletableFuture<GroupPlayer> getOrLoad(final UUID user) {
        final GroupPlayer cachedGroupPlayer = cache.get(user);
        if (cachedGroupPlayer != null) {
            return CompletableFuture.completedFuture(cachedGroupPlayer);
        }

        return repository.findAssignment(user).thenApply(groupAssignments -> {
            String resolvedName = null;

            if (groupAssignments.isPresent()) {
                GroupAssignment assignment = groupAssignments.get();
                if (assignment.expiresAt() == null || assignment.expiresAt().isAfter(Instant.now())) {
                    resolvedName = assignment.groupName();
                } else {
                    // Expired
                    repository.deleteAssignment(user);
                }
            }

            if (resolvedName == null) {
                resolvedName = DefaultGroup.name();
            }

            final GroupPlayer fresh = GroupPlayer.from(user, resolvedName, catalogCache::get);
            cache.put(user, fresh);
            return fresh;
        });
    }

    public GroupPlayer getIfPresent(UUID uuid) {
        return cache.get(uuid);
    }

    public void invalidate(UUID uuid) {
        cache.invalidate(uuid);
    }

    public void clear() {
        cache.clear();
    }
}
