package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.service.GroupService;
import com.github.manu585.manusgroups.util.Durations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class GrantGroupCommand extends BaseCommand {
    private final YamlConfiguration lang;
    private final GroupService service;
    private final GroupCatalogCache catalog;

    public GrantGroupCommand(YamlConfiguration lang, GroupService service, GroupCatalogCache catalog) {
        super("grant");

        this.lang = lang;
        this.service = service;
        this.catalog = catalog;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(lang.getString("Usage.Grant", "Usage: /groups grant <player> <group> <time>")));
            return;
        }

        Player target = Bukkit.getPlayerExact(args.getFirst());
        if (target == null) {
            sender.sendMessage(Component.text("Player not online.", TextColor.color(255, 0, 0)));
            return;
        }

        String groupName = args.get(1);
        if (catalog.get(groupName) == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(lang.getString("Group.NotFound", "Group not found.")));
            return;
        }

        Duration duration = null;
        if (args.size() >= 3) {
            try {
                duration = Durations.parse(args.get(2));
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Shitty formatting boss");
                return;
            }
        }

        service.setGroup(target.getUniqueId(), groupName, duration).thenRun(() -> sender.sendMessage(MiniMessage.miniMessage().deserialize(lang.getString("Assign.Granted", "Granted.")))).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
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
