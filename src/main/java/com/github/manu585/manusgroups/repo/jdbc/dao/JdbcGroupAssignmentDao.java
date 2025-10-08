package com.github.manu585.manusgroups.repo.jdbc.dao;

import com.github.manu585.manusgroups.domain.GroupAssignment;
import com.github.manu585.manusgroups.repo.jdbc.JdbcHelper;
import com.github.manu585.manusgroups.util.Uuids;
import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class JdbcGroupAssignmentDao extends JdbcHelper implements GroupAssignmentDao {
    public JdbcGroupAssignmentDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public @Nullable GroupAssignment findByUser(UUID user) throws SQLException {
        final String SQL = """
                SELECT user_uuid, group_name, expires_at
                FROM `group_assignments`
                WHERE user_uuid = ?
                LIMIT 1
                """;

        return queryOne(SQL, bind(ps -> setUuidBytes(ps, 1, user)),
                rs -> new GroupAssignment(
                        Uuids.toUuid(rs.getBytes("user_uuid")),
                        rs.getString("group_name"),
                        rs.getTimestamp("expires_at") == null ? null : rs.getTimestamp("expires_at").toInstant()
                )
        ).orElse(null);
    }

    @Override
    public void upsert(UUID uuid, String groupName, @Nullable Instant expiresAt) throws SQLException {
        final String SQL = """
                INSERT INTO `group_assignments` (user_uuid, group_name, expires_at)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  group_name = VALUES(group_name),
                  expires_at = VALUES(expires_at)
                """;

        update(SQL, bind(ps -> {
            setUuidBytes(ps, 1, uuid);
            ps.setString(2, groupName);
            setNullableTimestamp(ps, 3, expiresAt);
        }));
    }

    @Override
    public boolean deleteByUser(UUID user) throws SQLException {
        final String SQL = "DELETE FROM `group_assignments` WHERE user_uuid = ?";

        int rows = update(SQL, bind(ps -> setUuidBytes(ps, 1, user)));
        return rows > 0;
    }

    @Override
    public List<UUID> listUsersByGroup(String groupName) throws SQLException {
        final String SQL = """
                SELECT user_uuid
                FROM `group_assignments`
                WHERE group_name = ?
                """;

        return queryList(SQL,
                bind(ps -> ps.setString(1, groupName)),
                rs -> Uuids.toUuid(rs.getBytes("user_uuid"))
        );
    }

    @Override
    public List<GroupAssignment> listAllWithExpiry() throws SQLException {
        final String SQL = """
                SELECT user_uuid, group_name, expires_at
                FROM `group_assignments`
                WHERE expires_at IS NOT NULL
                """;

        return queryList(SQL,
                rs -> new GroupAssignment(
                        Uuids.toUuid(rs.getBytes("user_uuid")),
                        rs.getString("group_name"),
                        rs.getTimestamp("expires_at").toInstant()
                )
        );
    }
}
