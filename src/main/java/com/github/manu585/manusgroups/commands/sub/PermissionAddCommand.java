package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.cache.GroupPermissionCache;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.messaging.MessageService;
import com.github.manu585.manusgroups.messaging.Msg;
import com.github.manu585.manusgroups.permissions.PermissionService;
import com.github.manu585.manusgroups.repo.GroupRepository;
import org.bukkit.command.CommandSender;

import java.util.List;

public class PermissionAddCommand extends BaseCommand {
    private final MessageService messages;
    private final GroupRepository repository;
    private final GroupCatalogCache catalog;
    private final GroupPermissionCache permissionCache;
    private final PermissionService permissionService;

    public PermissionAddCommand(MessageService messages, GroupRepository repository, GroupCatalogCache catalog, GroupPermissionCache permissionCache, PermissionService permissionService) {
        super("permadd");

        this.messages = messages;
        this.repository = repository;
        this.catalog = catalog;
        this.permissionCache = permissionCache;
        this.permissionService = permissionService;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            messages.send(sender, "Usage.PermAdd");
            return;
        }

        final String group = args.getFirst();
        final String node = args.get(1);
        final boolean value = args.size() < 3 || Boolean.parseBoolean(args.get(2));

        if (catalog.get(group) == null) {
            messages.send(sender, "Group.NotFound", Msg.str("name", group));
            return;
        }

        repository.upsertPermission(group, node, value)
                .thenRun(() -> permissionCache.invalidate(group))
                .thenCompose(__ -> permissionService.refreshAllForGroup(group))
                .thenRun(() -> messages.send(sender, "Perm.Added", Msg.str("group", group), Msg.str("node", node), Msg.str("value", String.valueOf(value))));
    }

    @Override
    public List<String> tab (CommandSender sender, List<String> args) {
        return switch (args.size()) {
            case 1 -> catalog.snapshot().keySet().stream().sorted().toList();
            case 2 -> List.of("essentials.fly", "guru.patic", "nein.hier.ist.patrick");
            case 3 -> List.of("true", "false");
            default -> List.of();
        };
    }
}
