package com.github.manu585.manusgroups.repo;

import com.github.manu585.manusgroups.util.General;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public final class Database implements AutoCloseable {
    private final JavaPlugin plugin;
    private final DbExecutor executor;

    private HikariDataSource hikariDataSource;

    public Database(final JavaPlugin plugin, final DbExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
    }

    public void init(final String host, final int port, final String database, final String username, final String password) {
        plugin.getLogger().info("Initiating HikariCP Database...");
        String connectionUrl = "jdbc:mariadb://" + host + ":" + port + "/" + database + "?useUnicode=true";

        HikariConfig hikariConfig = getHikariConfig(username, password, connectionUrl);

        hikariConfig.addDataSourceProperty("cachePrepStmts", true);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        hikariDataSource = new HikariDataSource(hikariConfig);

        plugin.getLogger().info("HikariCP Database initiation successful!");
    }

    public Connection get() throws SQLException {
        if (this.hikariDataSource == null) {
            throw new IllegalStateException("DataSource not initialized!");
        }

        return this.hikariDataSource.getConnection();
    }

    public CompletableFuture<Void> migrateAsync() {
        plugin.getLogger().info("Started Async DB migration process...");

        return executor.run(() -> {
            try (Connection connection = get();
                 Statement statement = connection.createStatement()) {
                // USERS
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS `users` (
                          `uuid` BINARY(16) NOT NULL,
                          PRIMARY KEY (`uuid`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                        """);

                // GROUPS
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS `groups` (
                          `name` VARCHAR(36) NOT NULL,
                          `prefix` VARCHAR(36) NOT NULL,
                          `weight` INT NOT NULL,
                          `is_default` TINYINT(1) NOT NULL DEFAULT 0,
                          PRIMARY KEY (`name`),
                          INDEX `idx_groups_weight` (`weight` DESC)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                        """);

                // GROUP ASSIGNMENTS
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS `group_assignments` (
                          `user_uuid` BINARY(16) NOT NULL,
                          `group_name` VARCHAR(36) NOT NULL,
                          `expires_at` DATETIME(3) NULL DEFAULT NULL,
                          PRIMARY KEY (`user_uuid`),
                          KEY `idx_assign_expiry` (`expires_at`),
                          CONSTRAINT `fk_assign_user`
                            FOREIGN KEY (`user_uuid`) REFERENCES `users` (`uuid`)
                            ON DELETE CASCADE,
                          CONSTRAINT `fk_assign_group`
                            FOREIGN KEY (`group_name`) REFERENCES `groups` (`name`)
                            ON DELETE RESTRICT
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                        """);

                // GROUP PERMISSIONS
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS `group_permissions` (
                          `group_name` VARCHAR(36) NOT NULL,
                          `node` VARCHAR(128) NOT NULL,
                          `value` TINYINT(1) NOT NULL DEFAULT 1,
                          PRIMARY KEY (`group_name`, `node`),
                          CONSTRAINT `fk_gp_group`
                            FOREIGN KEY (`group_name`) REFERENCES `groups` (`name`)
                            ON DELETE CASCADE
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                        """);
            }
        }).whenComplete((__, ex) -> General.runSync(plugin, () -> {
            if (ex != null) {
                plugin.getLogger().severe("Database Migration failed!" + ex);
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            } else {
                plugin.getLogger().finest("Finished Database Migration process! Database usable!");
            }
        }));
    }

    private HikariConfig getHikariConfig(String username, String password, String connectionUrl) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("ManusGroupsPro-DB-Pool");
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl(connectionUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setConnectionTimeout(10_000);
        hikariConfig.setMaxLifetime(30 * 60_000);
        hikariConfig.setKeepaliveTime(30_000);
        return hikariConfig;
    }

    @Override
    public void close() {
        if (this.hikariDataSource != null) {
            this.hikariDataSource.close();
            this.hikariDataSource = null;
        }
    }

    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }
}
