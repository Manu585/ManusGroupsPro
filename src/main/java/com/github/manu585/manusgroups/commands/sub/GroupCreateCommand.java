package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.service.GroupService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class GroupCreateCommand extends BaseCommand {
    private final YamlConfiguration lang;
    private final GroupService groupService;
    private final GroupCatalogCache catalog;

    public GroupCreateCommand(YamlConfiguration lang, GroupService groupService, GroupCatalogCache catalog) {
        super("create");

        this.lang = lang;
        this.groupService = groupService;
        this.catalog = catalog;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.size() < 3) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(lang.getString("Usage.Create", "Usage: /groups create <name> <weight> <prefix>")));
            return;
        }

        final int weight;
        try {
            weight = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(lang.getString("Err.BadNumber", "Sike! That's the wrong number")));
            return;
        }

        String prefix = String.join(" ", args.subList(2, args.size()));

        final Group group = new Group(args.getFirst(), prefix, weight, false);
        groupService.upsertGroup(group)
                .thenRun(() -> catalog.put(group))
                .thenRun(() -> sender.sendMessage(MiniMessage.miniMessage().deserialize(lang.getString("Group.Created", "Group created!"))));
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
