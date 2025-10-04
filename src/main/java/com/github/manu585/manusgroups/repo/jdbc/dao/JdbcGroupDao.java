package com.github.manu585.manusgroups.repo.jdbc.dao;

import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.repo.jdbc.JdbcHelper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcGroupDao extends JdbcHelper implements GroupDao {
    public JdbcGroupDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public List<Group> listAll() throws SQLException {
        return query("""
            SELECT name, prefix, weight, is_default
            FROM `groups`
        """, JdbcGroupDao::mapGroup);
    }

    @Override
    public void upsert(Group group) throws SQLException {
        update("""
            INSERT INTO `groups` (name, prefix, weight, is_default)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE prefix = VALUES(prefix), weight = VALUES(weight), is_default = VALUES(is_default)
        """, group.name(), group.prefix(), group.weight(), group.isDefault());
    }

    @Override
    public boolean delete(String name) throws SQLException {
        return update("DELETE FROM `groups` WHERE name = ?", name) > 0;
    }

    private static Group mapGroup(ResultSet rs) throws SQLException {
        return new Group(
                rs.getString("name"),
                rs.getString("prefix"),
                rs.getInt("weight"),
                rs.getBoolean("is_default")
        );
    }
}
