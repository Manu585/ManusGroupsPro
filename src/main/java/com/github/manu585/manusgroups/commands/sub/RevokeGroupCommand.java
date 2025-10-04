package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.defaults.DefaultGroup;
import com.github.manu585.manusgroups.messaging.MessageService;
import com.github.manu585.manusgroups.messaging.Msg;
import com.github.manu585.manusgroups.service.GroupService;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class RevokeGroupCommand extends BaseCommand {
    private final MessageService messages;
    private final GroupService service;

    public RevokeGroupCommand(MessageService messages, GroupService service) {
        super("revoke");

        this.messages = messages;
        this.service = service;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            messages.send(sender, "Usage.Revoke");
            return;
        }

        Player target = Bukkit.getPlayerExact(args.getFirst());
        if (target == null) {
            messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", args.getFirst()));
            return;
        }

        service.load(target.getUniqueId())
                .thenCompose(snapshot -> {
                    final String currentGroup = (snapshot.getPrimaryGroup() == null) ? DefaultGroup.name() : snapshot.getPrimaryGroup().name();

                    return service.clearToDefault(target.getUniqueId())
                            .thenRun(() -> messages.send(sender, "Assign.Revoked",
                                    Msg.str("player", target.getName()),
                                    Msg.str("group", currentGroup)));
                });
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }

        return List.of();
    }
}
