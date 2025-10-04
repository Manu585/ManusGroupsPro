package com.github.manu585.manusgroups.commands;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * @see <a href="https://github.com/ProjectKorra/ProjectKorra/blob/master/core/src/com/projectkorra/projectkorra/command/PKCommand.java">ProjectKorra PKCommand.java</a>
 */
public abstract class BaseCommand implements SubCommand {
    private final String name;

    public BaseCommand(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        return Collections.emptyList();
    }
}
