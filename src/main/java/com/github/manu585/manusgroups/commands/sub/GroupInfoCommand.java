package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.domain.GroupAssignment;
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
import java.util.UUID;

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

        UUID uuid = target.getUniqueId();
        repository.findAssignment(target.getUniqueId()).thenAccept(opt -> {
            if (opt.isEmpty()) {
                if (self) {
                    messages.send(sender, "Info.Now");
                } else {
                    messages.send(sender, "Info.Other",
                            Msg.str("player", target.getName()),
                            Msg.str("group", "default"),
                            Msg.str("remaining", "permanent"));
                }
                return;
            }

            final GroupAssignment assignment = opt.get();
            final String group = assignment.groupName();
            final String remaining;

            if (assignment.expiresAt() == null) {
                remaining = "permanent";
            } else {
                if (assignment.expiresAt().isAfter(Instant.now())) {
                    remaining = "expired";
                } else {
                    remaining = Durations.formatCompact(Duration.between(Instant.now(), assignment.expiresAt()));
                }
            }

            if (self) {
                messages.send(sender, "Info.Self",
                        Msg.str("group", group),
                        Msg.str("remaining", remaining));
            } else {
                messages.send(sender, "Info.Other",
                        Msg.str("player", target.getName()),
                        Msg.str("group", group),
                        Msg.str("remaining", remaining));
            }
        });
    }
}
