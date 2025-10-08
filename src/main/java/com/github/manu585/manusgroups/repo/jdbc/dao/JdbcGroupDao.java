package com.github.manu585.manusgroups.repo.jdbc.dao;

import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.repo.jdbc.JdbcHelper;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class JdbcGroupDao extends JdbcHelper implements GroupDao {
    public JdbcGroupDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Group> listAll() throws SQLException {
        final String SQL = """
                SELECT name, prefix, weight, is_default
                FROM `groups`
                """;

        return queryList(SQL, rs -> new Group(
                rs.getString("name"),
                rs.getString("prefix"),
                rs.getInt("weight"),
                rs.getBoolean("is_default")
        ));
    }

    @Override
    public void upsert(Group group) throws SQLException {
        final String SQL = """
                INSERT INTO `groups` (name, prefix, weight, is_default)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                  prefix = VALUES(prefix),
                  weight = VALUES(weight),
                  is_default = VALUES(is_default)
                """;

        update(SQL, bind(ps -> {
            ps.setString(1, group.name());
            ps.setString(2, group.prefix());
            ps.setInt(3, group.weight());
            ps.setBoolean(4, group.isDefault());
        }));
    }

    @Override
    public boolean delete(String name) throws SQLException {
        final String SQL = "DELETE FROM `groups` WHERE name = ?";

        int rows = update(SQL, bind(ps -> ps.setString(1, name)));
        return rows > 0;
    }
}
