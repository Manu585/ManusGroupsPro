package com.github.manu585.manusgroups.configuration;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.configuration.configs.LanguageConfig;
import com.github.manu585.manusgroups.configuration.configs.MainConfig;
import com.github.manu585.manusgroups.database.DbExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ConfigManager {
    private final List<ManagedConfig> configs = new ArrayList<>();

    private final ManusGroups plugin;
    private final MainConfig mainConfig;
    private final LanguageConfig languageConfig;

    public ConfigManager(final ManusGroups plugin) {
        this.plugin = plugin;
        this.mainConfig = new MainConfig(plugin);
        this.languageConfig = new LanguageConfig(plugin);

        configs.addAll(List.of(mainConfig, languageConfig));
    }

    public void loadAll() {
        for (final ManagedConfig config : configs) {
            try {
                config.load();
            } catch (final Exception exception) {
                throw new RuntimeException("Failed to load " + config.fileName(), exception);
            }
        }
    }

    public void fillAll() {
        for (final ManagedConfig config : configs) {
            try {
                config.fill();
                config.save();
            } catch (final Exception exception) {
                plugin.getLogger().severe("Failed to fill config: " + config.fileName());
            }
        }
    }

    public void reloadAll() {
        for (final ManagedConfig config : configs) {
            try {
                config.reload();
            } catch (final Exception exception) {
                throw new RuntimeException("Failed to reload " + config.fileName(), exception);
            }
        }
    }

    public CompletableFuture<Void> prepareAllAsync(DbExecutor executor) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (ManagedConfig config : configs) {
            futures.add(config.prepareAsync(executor));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public LanguageConfig getLanguageConfig() {
        return languageConfig;
    }
}
