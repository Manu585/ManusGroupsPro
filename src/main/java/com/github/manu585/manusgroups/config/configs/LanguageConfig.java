package com.github.manu585.manusgroups.config.configs;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.config.BaseYamlConfig;
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
        yaml.addDefault("Chat.Format", "<prefix> <name>: <message>");

        // Generic
        yaml.addDefault("Err.NotPlayer", "<red>This command can only be used by players.</red>");
        yaml.addDefault("Err.BadNumber", "<red><value></red> is not a number.");
        yaml.addDefault("Err.Usage", "<yellow>Usage:</yellow> <usage>");

        // Groups
        yaml.addDefault("Group.Created", "<green>Group <name> saved.</green>");
        yaml.addDefault("Group.Deleted", "<green>Group <name> deleted.</green>");
        yaml.addDefault("Group.Exists", "<yellow>Group <name> already exists.</yellow>");
        yaml.addDefault("Group.NotFound", "<red>Group <name> not found.</red>");

        // Assignments
        yaml.addDefault("Assign.Granted", "<green>Granted <group> to <player><extra>.</green>");
        yaml.addDefault("Assign.Revoked", "<green>Revoked <group> from <player>.</green>");
        yaml.addDefault("Assign.Duration.Format", " for <duration>");

        // Usage strings
        yaml.addDefault("Usage.Create", "/groups create <name> <weight> <prefix...>");
        yaml.addDefault("Usage.Delete", "/groups delete <name>");
        yaml.addDefault("Usage.Grant",  "/groups grant <player> <group> [7d|3h|30m]");
        yaml.addDefault("Usage.Revoke", "/groups revoke <player> <group>");
    }

    public String getChatFormat() {
        return yaml.getString("Chat.Format", "<prefix> <name>: <message>");
    }
}
