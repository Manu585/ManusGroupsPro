package com.github.manu585.manusgroups;

import com.github.manu585.manusgroups.configuration.ConfigManager;
import com.github.manu585.manusgroups.database.Database;
import com.github.manu585.manusgroups.database.DbExecutor;
import com.github.manu585.manusgroups.util.General;
import org.bukkit.plugin.java.JavaPlugin;

public class ManusGroups extends JavaPlugin {
    private ConfigManager configManager;

    private DbExecutor executorService;
    private Database database;

    @Override
    public void onEnable() {
        getLogger().info("Booting up ManusGroupsPro...");

        // Configuration Management
        configManager = new ConfigManager(this);

        // Executor Thread Pool
        executorService = new DbExecutor();

        // Async IO -> DB Initiation -> DB Migration -> init Core functionality (Main Thread)
        configManager.prepareAllAsync(executorService)
                .thenRun(() -> database = new Database(this, executorService))
                .thenRun(() -> database.init(
                        configManager.getMainConfig().dbHost(),
                        configManager.getMainConfig().dbPort(),
                        configManager.getMainConfig().dbName(),
                        configManager.getMainConfig().dbUser(),
                        configManager.getMainConfig().dbPass()
                ))
                .thenCompose(__ -> database.migrateAsync())
                .thenRun(() -> General.runSync(this, this::initCore))
                .exceptionally(ex -> {
                    General.runSync(this, () -> {
                        getLogger().severe("Boot process of ManusGroupsPro failed! " + ex);
                        getServer().getPluginManager().disablePlugin(this);
                    });
                    return null;
                });
    }

    @Override
    public void onDisable() {
        if (executorService != null) {
            executorService.shutDown();
        }

        if (database != null) {
            database.close();
        }
    }

    private void initCore() {

    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DbExecutor getExecutorService() {
        return executorService;
    }

    public Database getDatabase() {
        return database;
    }
}
