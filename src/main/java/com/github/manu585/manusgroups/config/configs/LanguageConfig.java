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
        yaml.addDefault("Group.CreatedOrUpdated", "<yellow>Group <gray><name></gray> saved.</yellow>");
        yaml.addDefault("Group.Deleted",          "<yellow>Group <gray><name></gray> deleted. Users moved to default.</yellow>");
        yaml.addDefault("Group.Exists",           "<yellow>Group <gray><name></gray> already exists.</yellow>");
        yaml.addDefault("Group.NotFound",         "<red>Group <gray><name></gray> not found.</red>");

        // Assignments
        yaml.addDefault("Assign.Granted", "<yellow>Granted <gray><player></gray> group <gray><group></gray><permanent>.</yellow>");
        yaml.addDefault("Assign.Revoked", "<yellow>Revoked group <gray><group></gray> from <gray><player></gray>.</yellow>");

        // Info
        yaml.addDefault("Info.Self",  "<yellow>Your group:</yellow> <gray><group></gray> <gray>(</gray><yellow><remaining></yellow><gray>)</gray>");
        yaml.addDefault("Info.Other", "<yellow><player>'s group:</yellow> <gray><group></gray> <gray>(</gray><yellow><remaining></yellow><gray>)</gray>");
        yaml.addDefault("Info.None",  "<yellow>No group assignment found. default applies.</yellow>");

        // Reload
        yaml.addDefault("Reload.OK",    "<yellow>Configuration reloaded.</yellow>");
        yaml.addDefault("Reload.Error", "<red>Reload failed:</red> <error>");

        // Errors
        yaml.addDefault("Errors.PlayerNotOnline", "<red>Player <gray><player></gray> is not online.</red>");
        yaml.addDefault("Errors.BadNumber",       "<red>Invalid number:</red> <gray><value></gray>");
        yaml.addDefault("Errors.BadDuration",     "<red>Invalid duration:</red> <gray><value></gray>");
    }

    public String getChatFormat() {
        return yaml.getString("Chat.Format", "<prefix> <name>: <message>");
    }
}
