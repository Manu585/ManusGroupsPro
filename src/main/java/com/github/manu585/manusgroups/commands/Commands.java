package com.github.manu585.manusgroups.commands;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.commands.sub.GrantGroupCommand;
import com.github.manu585.manusgroups.commands.sub.GroupCreateCommand;
import com.github.manu585.manusgroups.commands.sub.GroupDeleteCommand;
import com.github.manu585.manusgroups.commands.sub.RevokeGroupCommand;
import com.github.manu585.manusgroups.service.GroupService;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Commands implements CommandExecutor, TabCompleter {
    private final Map<String, BaseCommand> commands = new HashMap<>();

    public Commands(JavaPlugin plugin, YamlConfiguration lang, GroupService service, GroupCatalogCache groupCatalog) {
        register(new GrantGroupCommand(lang, service, groupCatalog));
        register(new RevokeGroupCommand(lang, service));

        register(new GroupCreateCommand(lang, service, groupCatalog));
        register(new GroupDeleteCommand(lang, service, groupCatalog));

        final PluginCommand groupCommand = plugin.getCommand("groups");
        if (groupCommand == null) return;

        groupCommand.setExecutor(this);
        groupCommand.setTabCompleter(this);
    }

    private void register(BaseCommand command) {
        commands.put(command.getName().toLowerCase(Locale.ROOT), command);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) return false;
        final BaseCommand sub = commands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) return false;
        sub.execute(sender, Arrays.asList(args).subList(1, args.length));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return commands.keySet().stream()
                    .filter(s -> s.startsWith(prefix))
                    .sorted()
                    .toList();
        }

        final BaseCommand sub = commands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) return List.of();

        return sub.tab(sender, Arrays.asList(args).subList(1, args.length));
    }
}
