package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.manager.SignSelectionManager;
import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.spi.GroupSignService;
import com.github.manu585.manusgroups.service.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.manu585.manusgroups.util.General.isSign;

public class GroupSignCommand extends BaseCommand {
    private final GroupSignService signs;
    private final SignSelectionManager selectionManager;

    public GroupSignCommand(final JavaPlugin plugin, final MessageService messages, final GroupSignService signs, final SignSelectionManager selectionManager) {
        super("sign", plugin, messages);

        this.signs = signs;
        this.selectionManager = selectionManager;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            msg(sender, "Usage.Sign");
            return;
        }

        switch (args.getFirst().toLowerCase(Locale.ROOT)) {
            case "bind" -> handleBind(sender, args);
            case "unbind" -> handleUnbind(sender);
            default -> msg(sender, "Usage.Sign");
        }
    }

    private void handleBind(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player player)) {
            msg(sender, "Errors.PlayerNotOnline", Msg.str("player", "console"));
            return;
        }

        if (args.size() < 2) {
            msg(sender, "Usage.Sign");
            return;
        }

        final Player target = Bukkit.getPlayerExact(args.get(1));
        if (target == null) {
            msg(sender, "Errors.PlayerNotOnline", Msg.str("player", args.get(1)));
            return;
        }

        // If player is staring at a sign: bind now.
        final Block looked = player.getTargetBlockExact(6);
        if (isSign(looked)) {
            final Location loc = looked.getLocation();
            signs.bind(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), target.getUniqueId())
                    .thenRun(() -> msg(sender, "Sign.Bound", Msg.str("player", target.getName())));
        } else {
            selectionManager.select(player.getUniqueId(), target.getUniqueId());
            msg(sender, "Sign.Arm", Msg.str("player", target.getName()));
        }
    }

    private void handleUnbind(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            msg(sender, "Errors.PlayerNotOnline", Msg.str("player", "console"));
            return;
        }

        final Block block = player.getTargetBlockExact(6);
        if (!isSign(block)) {
            msg(sender, "Errors.LookAtSign");
            return;
        }

        final Location loc = block.getLocation();
        signs.unbind(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
                .thenAccept(removed -> msg(sender, removed ? "Sign.Unbound" : "Sign.NotFound"));
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        if (args.isEmpty()) return List.of("bind", "unbind");

        if (args.size() == 1) {
            return Stream.of("bind", "unbind").filter(s -> s.startsWith(args.getFirst().toLowerCase(Locale.ROOT))).toList();
        }

        if ("bind".equalsIgnoreCase(args.getFirst()) && args.size() == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args.get(1).toLowerCase(Locale.ROOT)))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}