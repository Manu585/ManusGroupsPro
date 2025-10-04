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

        // Usage strings
        yaml.addDefault("Usage.Grant",  "<gray>Usage:</gray> <yellow>/groups grant <player> <group> [time]</yellow>");
        yaml.addDefault("Usage.Revoke", "<gray>Usage:</gray> <yellow>/groups revoke <player></yellow>");
        yaml.addDefault("Usage.Create", "<gray>Usage:</gray> <yellow>/groups create <name> <weight> <prefix></yellow>");
        yaml.addDefault("Usage.Delete", "<gray>Usage:</gray> <yellow>/groups delete <name></yellow>");
        yaml.addDefault("Usage.Info",   "<gray>Usage:</gray> <yellow>/groups info [player]</yellow>");
        yaml.addDefault("Usage.Reload", "<gray>Usage:</gray> <yellow>/groups reload</yellow>");

        // Groups
        yaml.addDefault("Group.CreatedOrUpdated", "<green>Group <white><name></white> saved.</green>");
        yaml.addDefault("Group.Deleted",          "<green>Group <white><name></white> deleted; users moved to default.</green>");
        yaml.addDefault("Group.Exists",           "<yellow>Group <white><name></white> already exists.</yellow>");
        yaml.addDefault("Group.NotFound",         "<red>Group <white><name></white> not found.</red>");

        // Assignments
        yaml.addDefault("Assign.Granted", "<green>Granted <white><player></white> group <white><group></white><permanent>.</green>");
        yaml.addDefault("Assign.Revoked", "<yellow>Revoked group <white><group></white> from <white><player></white>.</yellow>");

        // Info
        yaml.addDefault("Info.Self",  "<gray>Your group:</gray> <white><group></white> <gray>(</gray><remaining><gray>)</gray>");
        yaml.addDefault("Info.Other", "<white><player></white>'s group: <white><group></white> <gray>(</gray><remaining><gray>)</gray>");
        yaml.addDefault("Info.None",  "<yellow>No group assignment found; default applies.</yellow>");

        // Reload
        yaml.addDefault("Reload.OK",    "<green>Configuration reloaded.</green>");
        yaml.addDefault("Reload.Error", "<red>Reload failed:</red> <error>");

        // Errors
        yaml.addDefault("Errors.PlayerNotOnline", "<red>Player <white><player></white> is not online.</red>");
        yaml.addDefault("Errors.BadNumber",       "<red>Invalid number:</red> <value>");
        yaml.addDefault("Errors.BadDuration",     "<red>Invalid duration:</red> <value>");
    }

    public String getChatFormat() {
        return yaml.getString("Chat.Format", "<prefix> <name>: <message>");
    }
}
