package com.github.manu585.manusgroups.config;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.config.configs.LanguageConfig;
import com.github.manu585.manusgroups.config.configs.MainConfig;
import com.github.manu585.manusgroups.repo.DbExecutor;
import com.github.manu585.manusgroups.util.General;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ConfigManager {
    private final List<ManagedConfig> configs = new ArrayList<>();

    private final MainConfig mainConfig;
    private final LanguageConfig languageConfig;

    public ConfigManager(final ManusGroups plugin) {
        this.mainConfig = new MainConfig(plugin);
        this.languageConfig = new LanguageConfig(plugin);

        configs.addAll(List.of(mainConfig, languageConfig));
    }

    public CompletableFuture<Void> prepareAllAsync(DbExecutor executor) {
        final List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (ManagedConfig config : configs) {
            futures.add(config.prepareAsync(executor));
        }

        return General.allDone(futures);
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public LanguageConfig getLanguageConfig() {
        return languageConfig;
    }
}
