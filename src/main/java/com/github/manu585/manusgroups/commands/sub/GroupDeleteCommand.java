package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.messaging.MessageService;
import com.github.manu585.manusgroups.messaging.Msg;
import com.github.manu585.manusgroups.service.GroupService;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;

public class GroupDeleteCommand extends BaseCommand {
    private final MessageService messages;
    private final GroupService groupService;
    private final GroupCatalogCache catalog;

    public GroupDeleteCommand(MessageService messages, GroupService groupService, GroupCatalogCache catalog) {
        super("delete");

        this.messages = messages;
        this.groupService = groupService;
        this.catalog = catalog;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            messages.send(sender, "Usage.Delete");
            return;
        }

        String groupName = args.getFirst();
        if (catalog.get(groupName) == null) {
            messages.send(sender, "Group.NotFound", Msg.str("name", groupName));
            return;
        }

        groupService.deleteGroupByReassigning(groupName)
                .thenRun(() -> messages.send(sender, "Group.Deleted", Msg.str("name", groupName)));
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            String p = args.getFirst().toLowerCase(Locale.ROOT);
            return catalog.snapshot().keySet().stream()
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(p))
                    .sorted()
                    .toList();
        }
        return List.of();
    }
}
