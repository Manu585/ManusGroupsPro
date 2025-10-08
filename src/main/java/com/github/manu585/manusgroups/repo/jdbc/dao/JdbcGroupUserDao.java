package com.github.manu585.manusgroups.repo.jdbc.dao;

import com.github.manu585.manusgroups.repo.jdbc.JdbcHelper;
import com.github.manu585.manusgroups.util.Uuids;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.UUID;

public class JdbcGroupUserDao extends JdbcHelper implements GroupUserDao {
    public JdbcGroupUserDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void insertIgnore(UUID uuid) throws SQLException {
        update("INSERT IGNORE INTO `users` (uuid) VALUES (?)", bind(ps -> setUuidBytes(ps, 1, uuid)));
    }
}
