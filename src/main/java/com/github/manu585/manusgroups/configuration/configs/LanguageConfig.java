package com.github.manu585.manusgroups.configuration.configs;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.configuration.BaseYamlConfig;
import org.jetbrains.annotations.NotNull;

public final class LanguageConfig extends BaseYamlConfig {
    public LanguageConfig(@NotNull ManusGroups plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String fileName() {
        return "language.yml";
    }

    @Override
    public void fill() {
        yaml.addDefault("Group.Added", "Successfully added group!");
    }
}
