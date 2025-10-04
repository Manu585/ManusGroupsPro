package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.messaging.MessageService;
import com.github.manu585.manusgroups.messaging.Msg;
import com.github.manu585.manusgroups.spi.ChatFormatService;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand extends BaseCommand {
    private final ManusGroups plugin;
    private final MessageService messages;
    private final ChatFormatService chatFormatService;

    public ReloadCommand(final ManusGroups plugin, final MessageService messages, final ChatFormatService chatFormatService) {
        super("reload");

        this.plugin = plugin;
        this.messages = messages;
        this.chatFormatService = chatFormatService;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        try {
            plugin.getConfigManager().getLanguageConfig().reload();
            messages.reload(plugin.getConfigManager().getLanguageConfig().yaml());

            String newFormat = plugin.getConfigManager().getLanguageConfig().getChatFormat();
            chatFormatService.updateFormat(newFormat);

            messages.send(sender, "Reload.OK");
        } catch (Exception exception) {
            messages.send(sender, "Reload.Error", Msg.str("error", String.valueOf(exception.getMessage())));
        }
    }
}
