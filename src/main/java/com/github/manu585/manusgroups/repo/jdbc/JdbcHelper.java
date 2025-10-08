package com.github.manu585.manusgroups.repo.jdbc;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class JdbcHelper {
    protected final DataSource dataSource;

    protected JdbcHelper(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    protected static Binder bind(Binder binder) {
        return binder;
    }

    protected <T> List<T> queryList(String sql, Binder binder, RowMapper<T> mapper) throws SQLException {
        try (Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {
            if (binder != null) {
                binder.bind(ps);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapper.map(rs));
                }
                return out;
            }
        }
    }

    protected <T> List<T> queryList(String sql, RowMapper<T> mapper) throws SQLException {
        return queryList(sql, null, mapper);
    }

    protected <T> Optional<T> queryOne(String sql, Binder binder, RowMapper<T> mapper) throws SQLException {
        List<T> list = queryList(sql, binder, mapper);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }

    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper) throws SQLException {
        return queryOne(sql, null, mapper);
    }

    protected int update(String sql, Binder binder) throws SQLException {
        try (Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql)) {
            if (binder != null) {
                binder.bind(ps);
            }

            return ps.executeUpdate();
        }
    }

    protected int update(String sql) throws SQLException {
        return update(sql, null);
    }

    protected long updateAndReturnKey(String sql, Binder binder) throws SQLException {
        try (Connection connection = dataSource.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (binder != null) {
                binder.bind(ps);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("No generated keys returned.");
            }
        }
    }

    protected static void setUuidBytes(PreparedStatement ps, int idx, java.util.UUID uuid) throws SQLException {
        ps.setBytes(idx, com.github.manu585.manusgroups.util.Uuids.toBytes(uuid));
    }

    protected static void setNullableTimestamp(PreparedStatement ps, int idx, java.time.Instant instant) throws SQLException {
        if (instant == null) ps.setNull(idx, Types.TIMESTAMP);
        else ps.setTimestamp(idx, Timestamp.from(instant));
    }
}
