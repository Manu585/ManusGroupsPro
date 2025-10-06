package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.service.MessageService;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand extends BaseCommand {
    private final ManusGroups plugin;

    public ReloadCommand(final ManusGroups plugin, final MessageService messages) {
        super("reload", plugin, messages);

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        plugin.reloadAsync().thenAccept(success -> {
            if (success) {
                msg(sender, "Reload.OK");
            } else {
                msg(sender, "Reload.Error");
            }
        });
    }
}
