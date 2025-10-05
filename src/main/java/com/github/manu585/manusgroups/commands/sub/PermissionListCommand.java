package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.cache.GroupPermissionCache;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.messaging.MessageService;
import com.github.manu585.manusgroups.messaging.Msg;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class PermissionListCommand extends BaseCommand {
    private final MessageService messages;
    private final GroupCatalogCache catalog;
    private final GroupPermissionCache permissionCache;

    public PermissionListCommand(MessageService messages, GroupCatalogCache catalog, GroupPermissionCache permissionCache) {
        super("permlist");

        this.messages = messages;
        this.catalog = catalog;
        this.permissionCache = permissionCache;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            messages.send(sender, "Usage.PermList");
            return;
        }

        final String group = args.getFirst();

        if (catalog.get(group) == null) {
            messages.send(sender, "Perm.ListEmpty", Msg.str("group", group));
            return;
        }

        permissionCache.getOrLoad(group).thenAccept(map -> {
            if (map.isEmpty()) {
                messages.send(sender, "Perm.ListEmpty", Msg.str("group", group));
                return;
            }

            messages.send(sender, "Perm.ListHeader", Msg.str("group", group));

            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry ->
                    messages.send(sender, "Perm.ListEntry", Msg.str("node", entry.getKey()), Msg.str("value", String.valueOf(entry.getValue()))));
        });
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        return (args.size() == 1) ? catalog.snapshot().keySet().stream().sorted().toList() : List.of();
    }
}
