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
import java.util.Locale;
import java.util.Map;

public class PermissionRemoveCommand extends BaseCommand {
    private final MessageService messages;
    private final GroupRepository repository;
    private final GroupCatalogCache catalogCache;
    private final GroupPermissionCache permissionCache;
    private final PermissionService permissionService;

    public PermissionRemoveCommand(MessageService messages, GroupRepository repository, GroupCatalogCache catalogCache, GroupPermissionCache permissionCache, PermissionService permissionService) {
        super("permremove");

        this.messages = messages;
        this.repository = repository;
        this.catalogCache = catalogCache;
        this.permissionCache = permissionCache;
        this.permissionService = permissionService;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            messages.send(sender, "Usage.PermRemove");
            return;
        }

        final String group = args.getFirst();
        final String node = args.get(1);

        if (catalogCache.get(group) == null) {
            messages.send(sender, "Group.NotFound", Msg.str("name", group));
            return;
        }

        repository.deletePermission(group, node)
                .thenRun(() -> permissionCache.invalidate(group))
                .thenCompose(__ -> permissionService.refreshAllForGroup(group))
                .thenRun(() -> messages.send(sender, "Perm.Removed", Msg.str("group", group), Msg.str("node", node)));
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        if (args.size() == 1) return catalogCache.snapshot().keySet().stream().sorted().toList();
        if (args.size() == 2) {
            final String groupName = args.getFirst();
            final String prefix = args.get(1).toLowerCase(Locale.ROOT);

            Map<String, Boolean> nodes = permissionCache.getIfPresent(groupName);
            return nodes.keySet().stream().filter(n -> n.toLowerCase(Locale.ROOT).startsWith(prefix)).sorted().toList();
        }
        return List.of();
    }
}
