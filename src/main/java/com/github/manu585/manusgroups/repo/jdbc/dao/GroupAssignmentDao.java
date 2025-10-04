package com.github.manu585.manusgroups.repo.jdbc.dao;

import com.github.manu585.manusgroups.domain.GroupAssignment;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface GroupAssignmentDao {
    @Nullable GroupAssignment findByUser(UUID user) throws SQLException;
    void upsert(UUID user, String groupName, @Nullable Instant expiresAt) throws SQLException;
    boolean deleteByUser(UUID user) throws SQLException;
    List<UUID> listUsersByGroup(String groupName) throws SQLException;
    List<GroupAssignment> listAllWithExpiry() throws SQLException;
}
