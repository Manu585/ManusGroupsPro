package com.github.manu585.manusgroups.cache;

import com.github.manu585.manusgroups.defaults.DefaultGroup;
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

        // Fetch GroupAssignment for given user
        return repository.findAssignment(user).thenCompose(assignment -> {

            // If expired, delete in DB and continue with null
            if (assignment != null && assignment.expiresAt() != null && !assignment.expiresAt().isAfter(Instant.now())) {
                return repository.deleteAssignment(user).handle((__, __ex) -> null);
            }

            // Else continue with a valid name or null
            return CompletableFuture.completedFuture(assignment);
        }).thenApply(finalAssignment -> {
            final String groupName = (finalAssignment == null) ? DefaultGroup.name() : finalAssignment.groupName();

            // Build snapshot and cache
            GroupPlayer fresh = GroupPlayer.from(user, groupName, catalogCache::get);
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
