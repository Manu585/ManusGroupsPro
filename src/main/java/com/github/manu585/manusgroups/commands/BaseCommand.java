package com.github.manu585.manusgroups.commands;

import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.util.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

/**
 * @see <a href="https://github.com/ProjectKorra/ProjectKorra/blob/master/core/src/com/projectkorra/projectkorra/command/PKCommand.java">ProjectKorra PKCommand.java</a>
 */
public abstract class BaseCommand implements SubCommand {
    private final String name;
    protected final JavaPlugin plugin;
    protected final MessageService messages;

    public BaseCommand(final String name, final JavaPlugin plugin, final MessageService messages) {
        this.name = name;
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }

    protected void msg(CommandSender sender, String key, Msg... placeholders) {
        messages.send(sender, key, placeholders);
    }
}
