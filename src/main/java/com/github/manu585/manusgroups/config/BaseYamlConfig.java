package com.github.manu585.manusgroups.config;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.repo.DbExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class BaseYamlConfig implements ManagedConfig {
    protected final ManusGroups plugin;

    protected File file;
    protected YamlConfiguration yaml;

    protected BaseYamlConfig(final @NotNull ManusGroups plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<Void> prepareAsync(DbExecutor executor) {
        return executor.run(() -> {
            Path data = plugin.getDataFolder().toPath();
            Files.createDirectories(data);

            Path path = data.resolve(fileName());
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

    @Override
    public void load() throws Exception {
        File data = plugin.getDataFolder();

        if (!data.exists() && !data.mkdirs()) {
            throw new IOException("Could not create data folder!");
        }

        this.file = new File(data, fileName());

        if (!file.exists()) {
            InputStream in = plugin.getResource(file.getName());
            if (in != null) {
                plugin.getLogger().info("File found in plugin jar: copying...");
                plugin.saveResource(file.getName(), false);
            } else {
                plugin.getLogger().info("No jar embed resource for " + file.getName() + ". creating new file");
                if (!file.createNewFile()) {
                    plugin.getLogger().warning("Failed to create file: " + file.getName());
                }
            }
        }

        // Load yaml into memory
        yaml = YamlConfiguration.loadConfiguration(file);
        yaml.options().copyDefaults(true);
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
