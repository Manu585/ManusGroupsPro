package com.github.manu585.manusgroups.repo.jdbc;

import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.domain.GroupAssignment;
import com.github.manu585.manusgroups.domain.SignRecord;
import com.github.manu585.manusgroups.repo.DbExecutor;
import com.github.manu585.manusgroups.repo.GroupRepository;
import com.github.manu585.manusgroups.repo.jdbc.dao.*;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Async Wrapper for DB executions using own Executor Service
 */
public class JdbcGroupRepository implements GroupRepository {
    private final GroupUserDao users;
    private final GroupDao groups;
    private final GroupAssignmentDao assignments;
    private final GroupPermissionDao permissions;
    private final GroupSignDao signs;

    private final DbExecutor executor;

    public JdbcGroupRepository(GroupUserDao users, GroupDao groups, GroupAssignmentDao assignment, GroupPermissionDao permissions, GroupSignDao signs, DbExecutor executor) {
        this.users = users;
        this.groups = groups;
        this.assignments = assignment;
        this.permissions = permissions;
        this.signs = signs;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<List<Group>> listGroups() {
        return supply(groups::listAll);
    }

    @Override
    public CompletableFuture<Void> upsertGroup(Group group) {
        return run(() -> groups.upsert(group));
    }

    @Override
    public CompletableFuture<Boolean> deleteGroup(String groupName) {
        return supply(() -> groups.delete(groupName));
    }

    @Override
    public CompletableFuture<GroupAssignment> findAssignment(UUID user) {
        return supply(() -> assignments.findByUser(user));
    }

    @Override
    public CompletableFuture<Void> upsertAssignment(UUID user, String group, @Nullable Instant expiresAt) {
        return run(() -> {
            users.insertIgnore(user);
            assignments.upsert(user, group, expiresAt);
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteAssignment(UUID user) {
        return supply(() -> assignments.deleteByUser(user));
    }

    @Override
    public CompletableFuture<List<UUID>> listUsersByGroup(String groupName) {
        return supply(() -> assignments.listUsersByGroup(groupName));
    }

    @Override
    public CompletableFuture<List<GroupAssignment>> listAllWithExpiry() {
        return supply(assignments::listAllWithExpiry);
    }

    @Override
    public CompletableFuture<Map<String, Boolean>> listPermissionsByGroup(String groupName) {
        return supply(() -> permissions.listByGroup(groupName));
    }

    @Override
    public CompletableFuture<Void> upsertPermission(String groupName, String node, boolean value) {
        return run(() -> permissions.upsert(groupName, node, value));
    }

    @Override
    public CompletableFuture<Boolean> deletePermission(String groupName, String node) {
        return supply(() -> permissions.delete(groupName, node));
    }

    @Override
    public CompletableFuture<Void> upsertSign(String world, int x, int y, int z, UUID target) {
        return run(() -> signs.upsert(world, x, y, z, target));
    }

    @Override
    public CompletableFuture<Boolean> deleteSignAt(String world, int x, int y, int z) {
        return supply(() -> signs.deleteAt(world, x, y, z));
    }

    @Override
    public CompletableFuture<Integer> deleteSignsByTarget(UUID target) {
        return supply(() -> signs.deleteByTarget(target));
    }

    @Override
    public CompletableFuture<@Nullable SignRecord> findSignAt(String world, int x, int y, int z) {
        return supply(() -> signs.findAt(world, x, y, z));
    }

    @Override
    public CompletableFuture<List<SignRecord>> listSignsByTarget(UUID target) {
        return supply(() -> signs.listByTarget(target));
    }

    @Override
    public CompletableFuture<List<SignRecord>> listAllSigns() {
        return supply(signs::listAll);
    }

    private <T> CompletableFuture<T> supply(DbCallable<T> task) {
        return executor.supply(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<Void> run(DbRunnable task) {
        return executor.run(() -> {
            try {
                task.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @FunctionalInterface
    private interface DbCallable<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    private interface DbRunnable {
        void run() throws Exception;
    }
}
