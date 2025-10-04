package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.cache.GroupCatalogCache;
import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.service.GroupService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Locale;

public class GroupDeleteCommand extends BaseCommand {
    private final YamlConfiguration lang;
    private final GroupService groupService;
    private final GroupCatalogCache catalog;

    public GroupDeleteCommand(YamlConfiguration lang, GroupService groupService, GroupCatalogCache catalog) {
        super("delete");

        this.lang = lang;
        this.groupService = groupService;
        this.catalog = catalog;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(lang.getString("Usage.Delete", "Usage: /groups delete <name>")));
            return;
        }

        String groupName = args.getFirst();
        if (catalog.get(groupName) == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(lang.getString("Group.NotFound", "Group nof found.")));
            return;
        }

        groupService.deleteGroupByReassigning(groupName)
                .thenRun(() -> sender.sendMessage("Group deleted and users moved to default."));
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
