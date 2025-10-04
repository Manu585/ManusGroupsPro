package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.service.GroupService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class RevokeGroupCommand extends BaseCommand {
    private final YamlConfiguration lang;
    private final GroupService service;

    public RevokeGroupCommand(YamlConfiguration lang, GroupService service) {
        super("revoke");

        this.lang = lang;
        this.service = service;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(lang.getString("Usage.Revoke", "Usage: /groups revoke <player> ")));
            return;
        }

        Player target = Bukkit.getPlayerExact(args.getFirst());
        if (target == null) {
            sender.sendMessage(Component.text("Player not online.", TextColor.color(255, 0, 0)));
            return;
        }

        service.clearToDefault(target.getUniqueId()).thenRun(() -> sender.sendMessage(MiniMessage.miniMessage().deserialize(lang.getString("Assign.Revoked", "Revoked."))));
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        if (args.isEmpty()) return List.of();
        if (args.size() == 1) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();

        return List.of();
    }
}
