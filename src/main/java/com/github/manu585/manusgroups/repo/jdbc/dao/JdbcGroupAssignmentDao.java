package com.github.manu585.manusgroups.repo.jdbc.dao;

import com.github.manu585.manusgroups.domain.GroupAssignment;
import com.github.manu585.manusgroups.repo.jdbc.JdbcHelper;
import com.github.manu585.manusgroups.util.Uuids;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class JdbcGroupAssignmentDao extends JdbcHelper implements GroupAssignmentDao {
    public JdbcGroupAssignmentDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public @Nullable GroupAssignment findByUser(UUID user) throws SQLException {
        return query("""
                SELECT user_uuid, group_name, expires_at
                FROM `group_assignments`
                WHERE user_uuid = ?
                LIMIT 1
                """, rs -> new GroupAssignment(
                        Uuids.toUuid(rs.getBytes("user_uuid")),
                        rs.getString("group_name"),
                        rs.getTimestamp("expires_at") == null ? null : rs.getTimestamp("expires_at").toInstant()
        ), (Object) Uuids.toBytes(user)).getFirst();
    }

    @Override
    public void upsert(UUID uuid, String groupName, @Nullable Instant expiresAt) throws SQLException {
        update("""
                INSERT INTO `group_assignments` (user_uuid, group_name, expires_at)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  group_name = VALUES(group_name),
                  expires_at = VALUES(expires_at)
             """, Uuids.toBytes(uuid), groupName, expiresAt == null ? null : Timestamp.from(expiresAt));
    }

    @Override
    public boolean deleteByUser(UUID user) throws SQLException {
        return update("DELETE FROM `group_assignments` WHERE user_uuid = ?", (Object) Uuids.toBytes(user)) > 0;
    }

    @Override
    public List<UUID> listUsersByGroup(String groupName) throws SQLException {
        return query("""
                SELECT user_uuid
                FROM `group_assignments`
                WHERE group_name = ?
                """, rs -> Uuids.toUuid(rs.getBytes("user_uuid")), groupName);
    }

    @Override
    public List<GroupAssignment> listAllWithExpiry() throws SQLException {
        return query("""
                SELECT user_uuid, group_name, expires_at
                FROM `group_assignments`
                WHERE expires_at IS NOT NULL
                """, rs -> new GroupAssignment(
                Uuids.toUuid(rs.getBytes("user_uuid")),
                        rs.getString("group_name"),
                        rs.getTimestamp("expires_at").toInstant()
        ));
    }
}
