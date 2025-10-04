package com.github.manu585.manusgroups.config;

import com.github.manu585.manusgroups.repo.DbExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface ManagedConfig {
    @NotNull String fileName();
    void load() throws Exception;
    void fill() throws Exception;
    void save() throws Exception;
    void reload() throws Exception;
    CompletableFuture<Void> prepareAsync(DbExecutor executor);
}
