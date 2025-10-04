package com.github.manu585.manusgroups.repo;

import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.domain.GroupAssignment;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GroupRepository {
    // Group Catalog
    CompletableFuture<List<Group>> listGroups();
    CompletableFuture<Void> upsertGroup(Group group);
    CompletableFuture<Boolean> deleteGroup(String groupName);

    // Assignments
    CompletableFuture<Optional<GroupAssignment>> findAssignment(UUID user);
    CompletableFuture<Void> upsertAssignment(UUID user, String groupName, @Nullable Instant expiresAt);
    CompletableFuture<Boolean> deleteAssignment(UUID user);

    CompletableFuture<List<UUID>> listUsersByGroup(String groupName);
    CompletableFuture<List<GroupAssignment>> listAllWithExpiry();
}
