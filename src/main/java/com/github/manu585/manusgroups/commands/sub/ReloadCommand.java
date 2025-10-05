package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.commands.BaseCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand extends BaseCommand {
    private final ManusGroups plugin;

    public ReloadCommand(final ManusGroups plugin) {
        super("reload");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        plugin.reloadAsync(sender);
    }
}
