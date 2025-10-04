package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.messaging.MessageService;
import com.github.manu585.manusgroups.messaging.Msg;
import com.github.manu585.manusgroups.service.GroupService;
import com.github.manu585.manusgroups.util.Durations;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class GrantGroupCommand extends BaseCommand {
    private final MessageService messages;
    private final GroupService service;
    private final GroupCatalogCache catalog;

    public GrantGroupCommand(MessageService messages, GroupService service, GroupCatalogCache catalog) {
        super("grant");

        this.messages = messages;
        this.service = service;
        this.catalog = catalog;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            messages.send(sender, "Usage.Grant");
            return;
        }

        Player target = Bukkit.getPlayerExact(args.getFirst());
        if (target == null) {
            messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", args.getFirst()));
            return;
        }

        String groupName = args.get(1);
        if (catalog.get(groupName) == null) {
            messages.send(sender, "Group.NotFound", Msg.str("name", groupName));
            return;
        }

        Duration duration = null;
        if (args.size() >= 3) {
            try {
                duration = Durations.parse(args.get(2));
            } catch (IllegalArgumentException e) {
                messages.send(sender, "Errors.BadDuration", Msg.str("value", args.get(2)));
                return;
            }
        }

        Duration finalDuration = duration;
        service.setGroup(target.getUniqueId(), groupName, duration).thenRun(() -> {
            boolean perm = (finalDuration == null);
            messages.send(sender, "Assign.Granted",
                    Msg.str("player", target.getName()),
                    Msg.str("group", groupName),
                    Msg.permanent(perm));
        });
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        return switch (args.size()) {
            case 1 -> Bukkit.getOnlinePlayers().stream().map(Player::getName).sorted().toList();
            case 2 -> {
                String p = args.get(1).toLowerCase(Locale.ROOT);

                yield catalog.snapshot().keySet().stream()
                        .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(p))
                        .sorted().toList();
            }
            case 3 -> List.of("69m", "1h", "6h7m", "1d", "5d3h2s");
            default -> List.of();
        };
    }
}
