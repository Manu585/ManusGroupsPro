package com.github.manu585.manusgroups.repo.jdbc.dao;

import java.sql.SQLException;
import java.util.UUID;

public interface GroupUserDao {
    void insertIgnore(UUID uuid) throws SQLException;
}
