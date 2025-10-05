package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.cache.GroupPermissionCache;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.util.Msg;
import com.github.manu585.manusgroups.service.spi.PermissionService;
import com.github.manu585.manusgroups.repo.GroupRepository;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PermissionCommand extends BaseCommand {
    private final MessageService messages;
    private final GroupRepository repository;
    private final GroupCatalogCache catalog;
    private final GroupPermissionCache permissionCache;
    private final PermissionService permissionService;

    public PermissionCommand(MessageService messages, GroupRepository repository, GroupCatalogCache catalog, GroupPermissionCache permissionCache, PermissionService permissionService) {
        super("permission");

        this.messages = messages;
        this.repository = repository;
        this.catalog = catalog;
        this.permissionCache = permissionCache;
        this.permissionService = permissionService;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            messages.send(sender, "Usage.Permission");
            return;
        }

        final String action = args.getFirst().toLowerCase(Locale.ROOT);
        switch (action) {
            case "add" -> handleAdd(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "list" -> handleList(sender, args);
            default -> messages.send(sender, "Usage.Permission");
        }
    }

    private void handleAdd(CommandSender sender, List<String> args) {
        if (args.size() < 3) {
            messages.send(sender, "Usage.PermAdd");
            return;
        }

        final String group = args.get(1);
        final String node = args.get(2);
        final boolean value = (args.size() < 4) || Boolean.parseBoolean(args.get(3));

        if (catalog.get(group) == null) {
            messages.send(sender, "Group.NotFound", Msg.str("name", group));
            return;
        }

        repository.upsertPermission(group, node, value)
                .thenRun(() -> permissionCache.invalidate(group))
                .thenCompose(__ -> permissionService.refreshAllForGroup(group))
                .thenRun(() -> messages.send(sender, "Perm.Added", Msg.str("group", group), Msg.str("node", node), Msg.str("value", String.valueOf(value))));
    }

    private void handleRemove(CommandSender sender, List<String> args) {
        if (args.size() < 3) {
            messages.send(sender, "Usage.PermRemove");
            return;
        }

        final String group = args.get(1);
        final String node = args.get(2);

        if (catalog.get(group) == null) {
            messages.send(sender, "Group.NotFound", Msg.str("name", group));
            return;
        }

        repository.deletePermission(group, node)
                .thenRun(() -> permissionCache.invalidate(group))
                .thenCompose(__ -> permissionService.refreshAllForGroup(group))
                .thenRun(() -> messages.send(sender, "Perm.Removed", Msg.str("group", group), Msg.str("node", node)));
    }

    private void handleList(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            messages.send(sender, "Usage.PermList");
            return;
        }

        final String group = args.get(1);

        if (catalog.get(group) == null) {
            messages.send(sender, "Group.NotFound", Msg.str("name", group));
            return;
        }

        permissionCache.getOrLoad(group).thenAccept(map -> {
            if (map == null || map.isEmpty()) {
                messages.send(sender, "Perm.ListEmpty", Msg.str("group", group));
                return;
            }

            messages.send(sender, "Perm.ListHeader", Msg.str("group", group));
            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> messages.send(sender, "Perm.ListEntry", Msg.str("node", entry.getKey()), Msg.str("value", String.valueOf(entry.getValue()))));
        });
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            return List.of("add", "remove", "list");
        }

        if (args.size() == 1) {
            final String input = args.getFirst().toLowerCase(Locale.ROOT);
            return Stream.of("add", "remove", "list").filter(s -> s.startsWith(input)).toList();
        }

        final String action = args.getFirst().toLowerCase(Locale.ROOT);

        if (args.size() == 2) {
            // group name
            final String input = args.get(1).toLowerCase(Locale.ROOT);
            return catalog.snapshot().keySet().stream().filter(s -> s.startsWith(input)).sorted().toList();
        }

        if ("add".equals(action)) {
            if (args.size() == 3) {
                // node suggestions for add
                final String input = args.get(2).toLowerCase(Locale.ROOT);
                return Stream.of("essentials.fly", "essentials.god").filter(s -> s.startsWith(input)).toList();
            }
            if (args.size() == 4) {
                final String input = args.get(3).toLowerCase(Locale.ROOT);
                return Stream.of("true", "false").filter(s -> s.startsWith(input)).toList();
            }
        }

        if ("remove".equals(action)) {
            if (args.size() == 3) {
                final String groupName = args.get(1);
                final String nodePrefix = args.get(2).toLowerCase(Locale.ROOT);
                final Map<String, Boolean> nodes = permissionCache.getIfPresent(groupName);

                if (nodes == null || nodes.isEmpty()) return List.of();

                return nodes.keySet().stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(nodePrefix)).sorted().collect(Collectors.toList());
            }
        }

        return List.of();
    }
}
