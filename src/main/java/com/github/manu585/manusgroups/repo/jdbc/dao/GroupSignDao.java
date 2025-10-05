package com.github.manu585.manusgroups.repo.jdbc.dao;

import com.github.manu585.manusgroups.domain.SignRecord;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface GroupSignDao {
    void upsert(String world, int x, int y, int z, UUID target) throws SQLException;
    boolean deleteAt(String world, int x, int y, int z) throws SQLException;
    List<SignRecord> listByTarget(UUID target) throws SQLException;
}
