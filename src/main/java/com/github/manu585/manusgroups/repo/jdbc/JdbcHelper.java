package com.github.manu585.manusgroups.repo.jdbc;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public abstract class JdbcHelper {
    protected final DataSource dataSource;

    protected JdbcHelper(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapper.map(rs));
                }
                return out;
            }
        }
    }

    protected int update(String sql, Object... params) throws SQLException {
        try (Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            int idx = i+1;

            if (p instanceof byte[] bytes) {
                ps.setBytes(idx, bytes);
            } else if (p instanceof Instant instant) {
                ps.setTimestamp(idx, Timestamp.from(instant));
            } else {
                ps.setObject(idx, p);
            }
        }
    }
}
