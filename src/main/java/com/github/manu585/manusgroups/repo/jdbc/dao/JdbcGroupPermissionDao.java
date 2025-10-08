package com.github.manu585.manusgroups.repo.jdbc.dao;

import com.github.manu585.manusgroups.repo.jdbc.JdbcHelper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcGroupPermissionDao extends JdbcHelper implements GroupPermissionDao {
    public JdbcGroupPermissionDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Map<String, Boolean> listByGroup(String groupName) throws SQLException {
        final String SQL = """
                SELECT node, value
                FROM `group_permissions`
                WHERE group_name = ?
                """;

        List<Map.Entry<String, Boolean>> rows = queryList(SQL, bind(ps -> ps.setString(1, groupName)),
                rs -> Map.entry(rs.getString("node"), rs.getBoolean("value"))
        );

        Map<String, Boolean> out = new HashMap<>(rows.size());
        for (Map.Entry<String, Boolean> entry : rows) {
            out.put(entry.getKey(), entry.getValue());
        }

        return out;
    }

    @Override
    public void upsert(String groupName, String node, boolean value) throws SQLException {
        final String SQL = """
                INSERT INTO `group_permissions` (group_name, node, value)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE value = VALUES(value)
                """;

        update(SQL, bind(
                ps -> {
                    ps.setString(1, groupName);
                    ps.setString(2, node);
                    ps.setBoolean(3, value);
                }
        ));
    }

    @Override
    public boolean delete(String groupName, String node) throws SQLException {
        final String SQL = "DELETE FROM `group_permissions` WHERE group_name=? AND node=?";

        int rows = update(SQL, bind(
                ps -> {
                    ps.setString(1, groupName);
                    ps.setString(2, node);
                })
        );

        return rows > 0;
    }
}
