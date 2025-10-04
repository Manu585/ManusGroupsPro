package com.github.manu585.manusgroups.repo.jdbc.dao;

import com.github.manu585.manusgroups.domain.Group;

import java.sql.SQLException;
import java.util.List;

public interface GroupDao {
    List<Group> listAll() throws SQLException;
    void upsert(Group group) throws SQLException;
    boolean delete(String groupName) throws SQLException;
}
