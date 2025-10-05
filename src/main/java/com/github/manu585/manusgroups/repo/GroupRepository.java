package com.github.manu585.manusgroups.repo;

import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.domain.GroupAssignment;
import com.github.manu585.manusgroups.domain.SignRecord;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GroupRepository {
    // Group Catalog
    CompletableFuture<List<Group>> listGroups();
    CompletableFuture<Void> upsertGroup(Group group);
    CompletableFuture<Boolean> deleteGroup(String groupName);
    CompletableFuture<List<UUID>> listUsersByGroup(String groupName);


    // Assignments
    CompletableFuture<@Nullable GroupAssignment> findAssignment(UUID user);
    CompletableFuture<Void> upsertAssignment(UUID user, String groupName, @Nullable Instant expiresAt);
    CompletableFuture<Boolean> deleteAssignment(UUID user);
    CompletableFuture<List<GroupAssignment>> listAllWithExpiry();

    // Group Permissions
    CompletableFuture<Map<String, Boolean>> listPermissionsByGroup(String groupName);
    CompletableFuture<Void> upsertPermission(String groupName, String node, boolean value);
    CompletableFuture<Boolean> deletePermission(String groupName, String node);

    // Group Signs
    CompletableFuture<Void> upsertSign(String world, int x, int y, int z, UUID target);
    CompletableFuture<Boolean> deleteSignAt(String world, int x, int y, int z);
    CompletableFuture<List<SignRecord>> listSignsByTarget(UUID target);
}
