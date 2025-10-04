package com.github.manu585.manusgroups.repo.jdbc;

import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.domain.GroupAssignment;
import com.github.manu585.manusgroups.repo.DbExecutor;
import com.github.manu585.manusgroups.repo.GroupRepository;
import com.github.manu585.manusgroups.repo.jdbc.dao.GroupAssignmentDao;
import com.github.manu585.manusgroups.repo.jdbc.dao.GroupDao;
import com.github.manu585.manusgroups.repo.jdbc.dao.UserDao;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Async Wrapper for DB executions using own Executor Service
 */
public class JdbcGroupRepository implements GroupRepository {
    private final UserDao users;
    private final GroupDao groups;
    private final GroupAssignmentDao assignments;
    private final DbExecutor executor;

    public JdbcGroupRepository(UserDao users, GroupDao groups, GroupAssignmentDao assignment, DbExecutor executor) {
        this.users = users;
        this.groups = groups;
        this.assignments = assignment;
        this.executor = executor;
    }

    @Override
    public CompletableFuture<List<Group>> listGroups() {
        return executor.supply(() -> {
            try {
                return groups.listAll();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> upsertGroup(Group group) {
        return executor.run(() -> {
            try {
                groups.upsert(group);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteGroup(String groupName) {
        return executor.supply(() -> {
            try {
                return groups.delete(groupName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Optional<GroupAssignment>> findAssignment(UUID user) {
        return executor.supply(() -> {
            try {
                return Optional.ofNullable(assignments.findByUser(user));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> upsertAssignment(UUID user, String group, @Nullable Instant expiresAt) {
        return executor.run(() -> {
            try {
                users.insertIgnore(user);
                assignments.upsert(user, group, expiresAt);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> deleteAssignment(UUID user) {
        return executor.supply(() -> {
            try {
                return assignments.deleteByUser(user);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<UUID>> listUsersByGroup(String groupName) {
        return executor.supply(() -> {
            try {
                return assignments.listUsersByGroup(groupName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<List<GroupAssignment>> listAllWithExpiry() {
        return executor.supply(() -> {
            try {
                return assignments.listAllWithExpiry();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
