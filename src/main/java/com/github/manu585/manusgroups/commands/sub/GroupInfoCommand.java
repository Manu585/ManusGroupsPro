package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.defaults.DefaultGroup;
import com.github.manu585.manusgroups.messaging.MessageService;
import com.github.manu585.manusgroups.messaging.Msg;
import com.github.manu585.manusgroups.repo.GroupRepository;
import com.github.manu585.manusgroups.util.Durations;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class GroupInfoCommand extends BaseCommand {
    private final MessageService messages;
    private final GroupRepository repository;

    public GroupInfoCommand(final MessageService messages, final GroupRepository repository) {
        super("info");

        this.messages = messages;
        this.repository = repository;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        final boolean self = args.isEmpty();
        final Player target;

        if (self) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", "console"));
                return;
            }

            target = player;
        } else {
            target = Bukkit.getPlayerExact(args.getFirst());
            if (target == null) {
                messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", args.getFirst()));
                return;
            }
        }

        repository.findAssignment(target.getUniqueId()).thenAccept(assignment -> {
            final String groupName;
            final String remaining;

            if (assignment == null) {
                groupName = DefaultGroup.name();
                remaining = "permanent";
            } else {
                groupName = assignment.groupName();
                remaining = (assignment.expiresAt() == null) ? "permanent" : Durations.formatCompact(Duration.between(Instant.now(), assignment.expiresAt()));
            }

            if (self) {
                messages.send(sender, "Info.Self",
                        Msg.str("group", groupName),
                        Msg.str("remaining", remaining));
            } else {
                messages.send(sender, "Info.Other",
                        Msg.str("player", target.getName()),
                        Msg.str("group", groupName),
                        Msg.str("remaining", remaining));
            }
        });
    }
}
