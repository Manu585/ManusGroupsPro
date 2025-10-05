package com.github.manu585.manusgroups.repo.jdbc.dao;

import java.sql.SQLException;
import java.util.Map;

public interface GroupPermissionDao {
    Map<String, Boolean> listByGroup(String groupName) throws SQLException;
    void upsert(String groupName, String node, boolean value) throws SQLException;
    boolean delete(String groupName, String node) throws SQLException;
}
