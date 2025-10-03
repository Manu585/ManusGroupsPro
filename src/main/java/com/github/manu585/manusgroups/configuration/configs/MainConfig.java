package com.github.manu585.manusgroups.configuration.configs;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.configuration.BaseYamlConfig;
import org.jetbrains.annotations.NotNull;

public final class MainConfig extends BaseYamlConfig {
    public MainConfig(@NotNull ManusGroups plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String fileName() {
        return "config.yml";
    }

    @Override
    public void fill() {
        yaml.addDefault("Storage.Host", "localhost");
        yaml.addDefault("Storage.Port", 3306);
        yaml.addDefault("Storage.Database", "groups");
        yaml.addDefault("Storage.Username", "root");
        yaml.addDefault("Storage.Password", "");
    }

    public String dbHost() {
        return yaml.getString("Storage.Host");
    }

    public int dbPort() {
        return yaml.getInt("Storage.Port");
    }

    public String dbName() {
        return yaml.getString("Storage.Database");
    }

    public String dbUser() {
        return yaml.getString("Storage.Username");
    }

    public String dbPass() {
        return yaml.getString("Storage.Password");
    }
}
