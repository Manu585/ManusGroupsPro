package com.github.manu585.manusgroups.config;

import com.github.manu585.manusgroups.repo.DbExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class BaseYamlConfig implements ManagedConfig {
    protected final JavaPlugin plugin;

    protected File file;
    protected YamlConfiguration yaml;

    protected BaseYamlConfig(final @NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Void> prepareAsync(DbExecutor executor) {
        return executor.run(() -> {
            final Path data = plugin.getDataFolder().toPath();
            Files.createDirectories(data);

            final Path path = data.resolve(fileName());
            this.file = path.toFile();

            if (Files.notExists(path)) {
                try (InputStream in = plugin.getClass().getClassLoader().getResourceAsStream(fileName())) {
                    if (in != null) {
                        Files.copy(in, path);
                    } else {
                        Files.createFile(path);
                    }
                }
            }

            this.yaml = YamlConfiguration.loadConfiguration(this.file);
            this.yaml.options().copyDefaults(true);

            fill();

            this.yaml.save(this.file);
        });
    }

    public abstract void fill() throws Exception;

    @Override
    public void save() throws Exception {
        if (yaml == null) throw new IllegalStateException("Can't save config! Config not loaded!");
        yaml.save(file);
    }

    @Override
    public void reload() throws Exception {
        yaml = YamlConfiguration.loadConfiguration(file);
        yaml.options().copyDefaults(true);
        fill();
        save();
    }

    public YamlConfiguration yaml() {
        return yaml;
    }
}
