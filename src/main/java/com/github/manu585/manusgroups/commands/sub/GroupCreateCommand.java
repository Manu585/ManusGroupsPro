package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.messaging.MessageService;
import com.github.manu585.manusgroups.messaging.Msg;
import com.github.manu585.manusgroups.service.GroupService;
import org.bukkit.command.CommandSender;

import java.util.List;

public class GroupCreateCommand extends BaseCommand {
    private final MessageService message;
    private final GroupService groupService;

    public GroupCreateCommand(MessageService message, GroupService groupService) {
        super("create");

        this.message = message;
        this.groupService = groupService;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() < 3) {
            message.send(sender, "Usage.Create");
            return;
        }

        final int weight;
        try {
            weight = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            message.send(sender, "Errors.BadNumber", Msg.str("value", args.get(1)));
            return;
        }

        final String prefix = String.join(" ", args.subList(2, args.size()));
        final Group group = new Group(args.getFirst(), prefix, weight, false);

        groupService.upsertGroup(group).thenRun(() -> message.send(sender, "Group.CreatedOrUpdated", Msg.str("name", args.getFirst())));
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        return switch (args.size()) {
            case 1 -> List.of("<Name>");
            case 2 -> List.of("<Weight>");
            case 3 -> List.of("<Prefix>");
            default -> List.of();
        };
    }
}
