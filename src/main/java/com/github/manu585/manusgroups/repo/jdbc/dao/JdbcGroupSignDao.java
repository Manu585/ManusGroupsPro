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
        update("""
                INSERT INTO `group_signs` (world, x, y, z, target_uuid)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE target_uuid = VALUES(target_uuid)
                """, world, x, y, z, Uuids.toBytes(target));
    }

    @Override
    public boolean deleteAt(String world, int x, int y, int z) throws SQLException {
        return update("DELETE FROM `group_signs` WHERE world=? AND x=? AND y=? AND z=?", world, x, y, z) > 0;
    }

    @Override
    public List<SignRecord> listByTarget(UUID target) throws SQLException {
        return query("""
                SELECT world, x, y, z, target_uuid
                FROM `group_signs`
                WHERE target_uuid=?
                """, rs -> new SignRecord(
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("z"),
                        Uuids.toUuid(rs.getBytes("target_uuid"))
        ), (Object) Uuids.toBytes(target));
    }
}
