package com.github.manu585.manusgroups.commands;

import com.github.manu585.manusgroups.ManusGroups;
import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.cache.GroupPermissionCache;
import com.github.manu585.manusgroups.commands.sub.GroupCommand;
import com.github.manu585.manusgroups.commands.sub.GroupSignCommand;
import com.github.manu585.manusgroups.commands.sub.PermissionCommand;
import com.github.manu585.manusgroups.commands.sub.ReloadCommand;
import com.github.manu585.manusgroups.manager.SignSelectionManager;
import com.github.manu585.manusgroups.repo.GroupRepository;
import com.github.manu585.manusgroups.service.GroupService;
import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.spi.GroupSignService;
import com.github.manu585.manusgroups.service.spi.PermissionService;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Commands implements CommandExecutor, TabCompleter {
    private final Map<String, BaseCommand> commands = new HashMap<>();

    public Commands(
            ManusGroups plugin,
            MessageService messageService,
            GroupService service,
            GroupRepository repository,
            GroupCatalogCache groupCatalog,
            GroupPermissionCache permissionCache,
            PermissionService permissionService,
            GroupSignService signService,
            SignSelectionManager selectionManager)
    {
        register(new GroupCommand(plugin, messageService, service, groupCatalog, repository));
        register(new ReloadCommand(plugin, messageService));
        register(new PermissionCommand(plugin, messageService, repository, groupCatalog, permissionCache, permissionService));
        register(new GroupSignCommand(plugin, messageService, signService, selectionManager));

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
        if (args.length == 0) {
            return false;
        }
        
        final BaseCommand sub = commands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            return false;
        }

        sub.execute(sender, Arrays.asList(args).subList(1, args.length));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return commands.keySet().stream().filter(s -> s.startsWith(prefix)).sorted().toList();
        }

        final BaseCommand sub = commands.get(args[0].toLowerCase(Locale.ROOT));
        if (sub == null) {
            return List.of();
        }

        return sub.tab(sender, Arrays.asList(args).subList(1, args.length));
    }
}
