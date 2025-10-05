package com.github.manu585.manusgroups.service;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.cache.GroupPlayerCache;
import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.domain.GroupPlayer;
import com.github.manu585.manusgroups.events.GroupChangeEvent;
import com.github.manu585.manusgroups.expiry.ExpiryScheduler;
import com.github.manu585.manusgroups.repo.GroupRepository;
import com.github.manu585.manusgroups.service.spi.GroupSignService;
import com.github.manu585.manusgroups.service.spi.PermissionService;
import com.github.manu585.manusgroups.service.spi.PrefixService;
import com.github.manu585.manusgroups.util.DefaultGroup;
import com.github.manu585.manusgroups.util.General;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Business Layer, mutations, ui, snapshots, ...
 */
public class GroupService {
    private final ManusGroups plugin;
    private final GroupRepository repository;
    private final GroupCatalogCache catalog;
    private final GroupPlayerCache players;
    private final PrefixService prefixes;
    private final PermissionService permissionService;
    private final GroupSignService signService;
    private final ExpiryScheduler expiry;

    public GroupService(final ManusGroups plugin,
                        final GroupRepository repository,
                        final GroupCatalogCache catalog,
                        final GroupPlayerCache players,
                        final PrefixService prefixes,
                        final PermissionService permissionService,
                        final GroupSignService signService,
                        final ExpiryScheduler expiry)
    {
        this.plugin = plugin;
        this.repository = repository;
        this.catalog = catalog;
        this.players = players;
        this.prefixes = prefixes;
        this.permissionService = permissionService;
        this.signService = signService;
        this.expiry = expiry;
    }

    /* ==========================
     * Public, high level API
     * ========================== */

    /**
     * Create or update a catalog group, update in memory cache immediately,
     * then refresh online player currently using that group.
     */
    public CompletableFuture<Void> upsertGroup(Group group) {
        return repository.upsertGroup(group)
                .thenRun(() -> catalog.put(group))
                .thenCompose(__ -> refreshPlayersUsingGroup(group.name()));
    }

    /**
     * Assign a player to a group.
     * @param user UUID of user to assign
     * @param groupName Name of group
     * @param duration Optional duration, if null assignment is permanent
     */
    public CompletableFuture<Void> setGroup(UUID user, String groupName, @Nullable Duration duration) {
        final Instant expiresAt = toExpiresAt(duration);

        return repository.upsertAssignment(user, groupName, expiresAt)
                .thenRun(() -> expiry.scheduleOrCancel(user, expiresAt))
                .thenCompose(__ -> refreshPlayerSnapshot(user))
                .thenCompose(__ -> permissionService.applyFor(user, groupName))
                .thenCompose(__ -> refreshPlayerUi(user))
                .thenRun(() -> fireGroupChange(user, catalog.get(groupName)))
                .thenRun(() -> signService.refreshFor(user));
    }

    /**
     * Revoke old group -> give player the default group
     * @param user UUID of user to give the group to
     */
    public CompletableFuture<Void> clearToDefault(UUID user) {
        return setGroup(user, DefaultGroup.name(), null).thenRun(() -> expiry.scheduleOrCancel(user, null));
    }

    /**
     * Load or rebuild a players GroupPlayer snapshot (async)
     * @param user UUID of person to load
     */
    public CompletableFuture<GroupPlayer> load(UUID user) {
        return players.getOrLoad(user);
    }

    public CompletableFuture<Void> ensureDefaultPersisted(UUID user) {
        return repository.findAssignment(user)
                .thenCompose(assignment -> {
                    if (assignment != null) {
                        return CompletableFuture.completedFuture(null);
                    }

                    return repository.upsertAssignment(user, DefaultGroup.name(), null)
                            .thenCompose(__ -> refreshPlayerSnapshot(user))
                            .thenCompose(__ -> refreshPlayerUi(user));
                });
    }

    /**
     * Delete a group, reassigning affected users to default then deleting group from cache as well
     * @param groupName Group to delete
     */
    public CompletableFuture<Void> deleteGroupByReassigning(String groupName) {
        return repository.listUsersByGroup(groupName)
                .thenCompose(affected -> {
                    final List<CompletableFuture<?>> ops = new ArrayList<>();
                    for (UUID uuid : affected) {
                        ops.add(setGroup(uuid, DefaultGroup.name(), null));
                    }
                    return all(ops);
                })
                .thenCompose(__ -> repository.deleteGroup(groupName))
                .thenRun(() -> catalog.invalidate(groupName));
    }

    public void invalidateCachesFor(UUID user) {
        players.invalidate(user);
        prefixes.invalidate(user);
        permissionService.clear(user);
    }

    /* =====================
     *  Private helper API
     * ===================== */

    /**
     * Convert duration to absolute expiry Instant, null if permanent
     *
     * @param duration Duration of Assignment
     * @return Absolute expiry Instant
     */
    private static @Nullable Instant toExpiresAt(@Nullable Duration duration) {
        return (duration == null) ? null : Instant.now().plus(duration);
    }

    /**
     * Invalidate and rebuild a users GroupPlayer snapshot (async)
     * @param user User to refresh
     */
    private CompletableFuture<Void> refreshPlayerSnapshot(UUID user) {
        players.invalidate(user);
        return players.getOrLoad(user).thenApply(__ -> null);
    }

    /**
     * Refreshes a users UI:
     * <ul>
     *    <li>prime the prefix cache (async)</li>
     *    <li>apply display name / tablist / team prefix on the main thread</li>
     * </ul>
     *
     * @param user UUID of user to refresh
     */
    private CompletableFuture<Void> refreshPlayerUi(UUID user) {
        return prefixes.primePrefix(user).thenRun(() -> {
            final Player player = plugin.getServer().getPlayer(user);
            if (player != null) {
                runOnMain(() -> prefixes.refreshDisplayName(player));
            }
        });
    }

    /**
     * Find online players currently using a given group and refresh them
     * 
     * @param groupName Name of the group to check
     */
    private CompletableFuture<Void> refreshPlayersUsingGroup(String groupName) {
        final List<CompletableFuture<?>> tasks = new ArrayList<>();
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            final UUID uuid = player.getUniqueId();
            final GroupPlayer snapshot = players.getIfPresent(uuid);
            if (snapshot == null || !snapshot.hasGroup(groupName)) continue;

            tasks.add(refreshPlayerSnapshot(uuid).thenCompose(__ -> refreshPlayerUi(uuid)));
        }

        return all(tasks);
    }


    /**
     * Fires the {@link GroupChangeEvent}
     *
     * @param user UUID of user
     * @param group Users new group if applicable
     */
    private void fireGroupChange(UUID user, @Nullable Group group) {
        runOnMain(() -> plugin.getServer().getPluginManager().callEvent(new GroupChangeEvent(user, group)));
    }

    /**
     * allOf wrapper with empty safe behaviour
     */
    private static CompletableFuture<Void> all(List<CompletableFuture<?>> tasks) {
        if (tasks.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
    }

    /**
     * Fast Main Thread
     */
    private void runOnMain(Runnable r) {
        General.runSync(plugin, r);
    }
}
