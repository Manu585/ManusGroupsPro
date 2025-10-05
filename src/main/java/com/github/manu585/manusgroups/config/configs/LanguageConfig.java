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

        // Usage
        yaml.addDefault("Usage.Grant",        "<gray>Usage:</gray> <yellow>/groups grant <player> <group> [time]</yellow>");
        yaml.addDefault("Usage.Revoke",       "<gray>Usage:</gray> <yellow>/groups revoke <player></yellow>");
        yaml.addDefault("Usage.Create",       "<gray>Usage:</gray> <yellow>/groups create <name> <weight> <prefix></yellow>");
        yaml.addDefault("Usage.Delete",       "<gray>Usage:</gray> <yellow>/groups delete <name></yellow>");
        yaml.addDefault("Usage.Info",         "<gray>Usage:</gray> <yellow>/groups info [player]</yellow>");
        yaml.addDefault("Usage.Reload",       "<gray>Usage:</gray> <yellow>/groups reload</yellow>");
        yaml.addDefault("Usage.PermAdd",      "<gray>Usage:</gray> <yellow>/groups permadd <group> <node> [true|false]</yellow>");
        yaml.addDefault("Usage.PermRemove",   "<gray>Usage:</gray> <yellow>/groups permremove <group> <node></yellow>");
        yaml.addDefault("Usage.PermList",     "<gray>Usage:</gray> <yellow>/groups permlist <group></yellow>");
        yaml.addDefault("Usage.Sign",         "<gray>Usage:</gray> <yellow>/groups sign bind <player> | /groups sign unbind</yellow>");
        yaml.addDefault("Usage.SignBindAt",   "<gray>Usage:</gray> <yellow>/groups sign bindat <player> <world> <x> <y> <z></yellow>");
        yaml.addDefault("Usage.SignUnbindAt", "<gray>Usage:</gray> <yellow>/groups sign unbindat <world> <x> <y> <z></yellow>");

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
        yaml.addDefault("Info.None",  "<yellow>No group assignment found. Default applies.</yellow>");

        // Reload
        yaml.addDefault("Reload.OK",    "<yellow>Configuration reloaded.</yellow>");
        yaml.addDefault("Reload.Error", "<red>Reload failed:</red> <error>");

        // Permissions
        yaml.addDefault("Perm.Added",     "<yellow>Added permission</yellow> <gray><node></gray> <gray>=</gray> <gray><value></gray> <yellow>to group</yellow> <gray><group></gray>.");
        yaml.addDefault("Perm.Removed",   "<yellow>Removed permission</yellow> <gray><node></gray> <yellow>from group</yellow> <gray><group></gray>.");
        yaml.addDefault("Perm.ListHeader","<gray>Permissions for group</gray> <gray><group></gray>:");
        yaml.addDefault("Perm.ListEntry", " - <gray><node></gray> <gray>=</gray> <gray><value></gray>");
        yaml.addDefault("Perm.ListEmpty", "<yellow>No permissions set for group</yellow> <gray><group></gray>.");
        yaml.addDefault("Perm.Error",     "<red>Permissions error:</red> <error>");

        // Errors
        yaml.addDefault("Errors.PlayerNotOnline", "<red>Player <gray><player></gray> is not online.</red>");
        yaml.addDefault("Errors.BadNumber",       "<red>Invalid number:</red> <gray><value></gray>");
        yaml.addDefault("Errors.BadDuration",     "<red>Invalid duration:</red> <gray><value></gray>");
        yaml.addDefault("Errors.LookAtSign",      "<red>Please look directly at a sign block.</red>");
        yaml.addDefault("Errors.WorldNotFound",   "<red>World not found:</red> <gray><world></gray>");

        // Signs (messages)
        yaml.addDefault("Sign.Bound",    "<yellow>Sign bound to <gray><player></gray>.</yellow>");
        yaml.addDefault("Sign.Unbound",  "<yellow>Sign unbound.</yellow>");
        yaml.addDefault("Sign.NotFound", "<red>No sign binding found here.</red>");
        yaml.addDefault("Sign.Error",    "<red>Sign operation failed:</red> <error>");

        // Signs (lines)
        yaml.addDefault("Signs.Format.Line1", "<prefix>");
        yaml.addDefault("Signs.Format.Line2", "<yellow><player></yellow>");
        yaml.addDefault("Signs.Format.Line3", "<gray>Group:</gray> <yellow><group></yellow>");
        yaml.addDefault("Signs.Format.Line4", "");
    }

    public String getChatFormat() {
        return yaml.getString("Chat.Format", "<prefix> <name>: <message>");
    }
}
