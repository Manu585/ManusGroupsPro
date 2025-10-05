package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.util.DefaultGroup;
import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.util.Msg;
import com.github.manu585.manusgroups.repo.GroupRepository;
import com.github.manu585.manusgroups.service.GroupService;
import com.github.manu585.manusgroups.util.Durations;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupCommand extends BaseCommand {
    private final MessageService messages;
    private final GroupService groupService;
    private final GroupCatalogCache catalog;
    private final GroupRepository repository;

    public GroupCommand(MessageService messages, GroupService groupService, GroupCatalogCache catalog, GroupRepository repository) {
        super("group");
        this.messages = messages;
        this.groupService = groupService;
        this.catalog = catalog;
        this.repository = repository;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            messages.send(sender, "Usage.Create");
            messages.send(sender, "Usage.Delete");
            messages.send(sender, "Usage.Info");
            messages.send(sender, "Usage.Grant");
            messages.send(sender, "Usage.Revoke");
            return;
        }

        final String action = args.getFirst().toLowerCase(Locale.ROOT);
        switch (action) {
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "info"   -> handleInfo(sender, args);
            case "grant"  -> handleGrant(sender, args);
            case "revoke" -> handleRevoke(sender, args);
            default -> {
                messages.send(sender, "Usage.Create");
                messages.send(sender, "Usage.Delete");
                messages.send(sender, "Usage.Info");
                messages.send(sender, "Usage.Grant");
                messages.send(sender, "Usage.Revoke");
            }
        }
    }

    private void handleCreate(CommandSender sender, List<String> args) {
        if (args.size() < 4) {
            messages.send(sender, "Usage.Create");
            return;
        }

        final String groupName = args.get(1);
        final int weight;
        try {
            weight = Integer.parseInt(args.get(2));
        } catch (NumberFormatException exception) {
            messages.send(sender, "Errors.BadNumber", Msg.str("value", args.get(2)));
            return;
        }

        final String prefix = String.join(" ", args.subList(3, args.size()));
        final Group group = new Group(groupName, prefix, weight, false);

        groupService.upsertGroup(group).thenRun(() -> messages.send(sender, "Group.CreatedOrUpdated", Msg.str("name", groupName)));
    }

    private void handleDelete(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            messages.send(sender, "Usage.Delete");
            return;
        }

        final String groupName = args.get(1);
        if (catalog.get(groupName) == null) {
            messages.send(sender, "Group.NotFound", Msg.str("name", groupName));
            return;
        }

        groupService.deleteGroupByReassigning(groupName).thenRun(() -> messages.send(sender, "Group.Deleted", Msg.str("name", groupName)));
    }

    private void handleInfo(CommandSender sender, List<String> args) {
        final boolean self = args.size() == 1;
        final Player target;

        if (self) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", "console"));
                return;
            }
            target = player;
        } else {
            target = Bukkit.getPlayerExact(args.get(1));
            if (target == null) {
                messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", args.get(1)));
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
                messages.send(sender, "Info.Self", Msg.str("group", groupName), Msg.str("remaining", remaining));
            } else {
                messages.send(sender, "Info.Other", Msg.str("player", target.getName()), Msg.str("group", groupName), Msg.str("remaining", remaining));
            }
        });
    }

    private void handleGrant(CommandSender sender, List<String> args) {
        if (args.size() < 3) {
            messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", args.get(1)));
            return;
        }

        final Player target = Bukkit.getPlayerExact(args.get(1));
        if (target == null) {
            messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", args.get(1)));
            return;
        }

        final String groupName = args.get(2);
        if (catalog.get(groupName) == null) {
            messages.send(sender, "Group.NotFound", Msg.str("name", groupName));
            return;
        }

        Duration duration = null;
        if (args.size() >= 4) {
            try {
                duration = Durations.parse(args.get(3));
            } catch (IllegalArgumentException e) {
                messages.send(sender, "Errors.BadNumber", Msg.str("value", args.get(3)));
                return;
            }
        }

        final Duration finalDuration = duration;
        groupService.setGroup(target.getUniqueId(), groupName, duration).thenRun(() -> {
            final boolean perm = (finalDuration == null);
            messages.send(sender, "Assign.Granted", Msg.str("player", target.getName()), Msg.str("group", groupName), Msg.permanent(perm));
        });
    }

    private void handleRevoke(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            messages.send(sender, "Usage.Revoke");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args.get(1));
        if (target == null) {
            messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", args.get(1)));
            return;
        }

        groupService.load(target.getUniqueId()).thenCompose(groupPlayer -> {
            final String currentGroup = (groupPlayer.getPrimaryGroup() == null) ? DefaultGroup.name() : groupPlayer.getPrimaryGroup().name();

            return groupService.clearToDefault(target.getUniqueId()).thenRun(() -> messages.send(sender, "Assign.Revoked", Msg.str("player", target.getName()), Msg.str("group", currentGroup)));
        });
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            return List.of("create", "delete", "info", "grant", "revoke");
        }

        if (args.size() == 1) {
            final String input = args.getFirst().toLowerCase(Locale.ROOT);

            return Stream.of("create", "delete", "info", "grant", "revoke").filter(s -> s.startsWith(input)).toList();
        }

        final String action = args.get(0).toLowerCase(Locale.ROOT);

        switch (action) {
            case "create" -> {
                // /groups group create <name> <weight> <prefix...>
                return switch (args.size()) {
                    case 2 -> List.of("<Name>");
                    case 3 -> List.of("<Weight>");
                    default -> List.of("<Prefix>");
                };
            }

            case "delete" -> {
                // /groups group delete <name>
                if (args.size() == 2) {
                    final String input = args.get(1).toLowerCase(Locale.ROOT);

                    return catalog.snapshot().keySet().stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(input)).sorted().toList();
                }
                return List.of();
            }

            case "info" -> {
                // /groups group info [player]
                if (args.size() == 2) {
                    final String input = args.get(1).toLowerCase(Locale.ROOT);

                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s -> s.toLowerCase(Locale.ROOT).startsWith(input)).sorted().collect(Collectors.toList());
                }
                return List.of();
            }

            case "grant" -> {
                // /groups group grant <player> <group> [time]
                if (args.size() == 2) {
                    final String input = args.get(1).toLowerCase(Locale.ROOT);

                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s -> s.toLowerCase(Locale.ROOT).startsWith(input)).sorted().collect(Collectors.toList());
                }

                if (args.size() == 3) {
                    final String input = args.get(2).toLowerCase(Locale.ROOT);

                    return catalog.snapshot().keySet().stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(input)).sorted().toList();
                }
                if (args.size() == 4) {
                    final String input = args.get(3).toLowerCase(Locale.ROOT);

                    return Stream.of("69m", "1h", "6h7m", "1d", "5d3h2s").filter(s -> s.startsWith(input)).toList();
                }
                return List.of();
            }

            case "revoke" -> {
                // /groups group revoke <player>
                if (args.size() == 2) {
                    final String input = args.get(1).toLowerCase(Locale.ROOT);
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase(Locale.ROOT).startsWith(input)).sorted().collect(Collectors.toList());
                }
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }
}

