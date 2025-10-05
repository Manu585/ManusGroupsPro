package com.github.manu585.manusgroups.service.spi;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PermissionService {
    /**
     * Apply the groups permission to a single user and replace all previously applied nodes
     * @param user UUID of user
     * @param groupName Name of group
     */
    CompletableFuture<Void> applyFor(final UUID user, final String groupName);

    /**
     * Remove any attachment created for this user
     * @param user UUID of user
     */
    CompletableFuture<Void> clear(final UUID user);

    /**
     * Re-apply permissions to all online players that currently have this group
     * @param groupName Name of group
     */
    CompletableFuture<Void> refreshAllForGroup(final String groupName);

    CompletableFuture<Void> refreshFor(final UUID uuid);
}
