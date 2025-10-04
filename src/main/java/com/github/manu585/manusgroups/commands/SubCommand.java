package com.github.manu585.manusgroups.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @see <a href="https://github.com/ProjectKorra/ProjectKorra/blob/master/core/src/com/projectkorra/projectkorra/command/SubCommand.java">ProjectKorra SubCommand.java</a>
 */
public interface SubCommand {
    String getName();
    void execute(CommandSender sender, List<String> args);
    List<String> tab(CommandSender sender, List<String> args);
}
