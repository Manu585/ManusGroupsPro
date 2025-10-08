package com.github.manu585.manusgroups.repo.jdbc.dao;

import com.github.manu585.manusgroups.domain.SignRecord;
import com.github.manu585.manusgroups.repo.jdbc.JdbcHelper;
import com.github.manu585.manusgroups.util.Uuids;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class JdbcGroupSignDao extends JdbcHelper implements GroupSignDao{
    public JdbcGroupSignDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void upsert(String world, int x, int y, int z, UUID target) throws SQLException {
        final String SQL = """
                INSERT INTO `group_signs` (world, x, y, z, target_uuid)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE target_uuid = VALUES(target_uuid)
                """;

        update(SQL, bind(ps -> {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
            setUuidBytes(ps, 5, target);
        }));
    }

    @Override
    public boolean deleteAt(String world, int x, int y, int z) throws SQLException {
        final String SQL = "DELETE FROM `group_signs` WHERE world=? AND x=? AND y=? AND z=?";

        int rows = update(SQL, bind(ps -> {
            ps.setString(1, world);
            ps.setInt(2, x);
            ps.setInt(3, y);
            ps.setInt(4, z);
        }));

        return rows > 0;
    }

    @Override
    public List<SignRecord> listByTarget(UUID target) throws SQLException {
        final String SQL = """
                SELECT world, x, y, z, target_uuid
                FROM `group_signs`
                WHERE target_uuid=?
                """;

        return queryList(SQL,
                bind(ps -> setUuidBytes(ps, 1, target)),
                rs -> new SignRecord(
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        Uuids.toUuid(rs.getBytes("target_uuid"))
                )
        );
    }
}
