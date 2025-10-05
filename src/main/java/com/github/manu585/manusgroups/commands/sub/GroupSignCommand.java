package com.github.manu585.manusgroups.commands.sub;

import com.github.manu585.manusgroups.commands.BaseCommand;
import com.github.manu585.manusgroups.messaging.MessageService;
import com.github.manu585.manusgroups.messaging.Msg;
import com.github.manu585.manusgroups.signs.GroupSignService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupSignCommand extends BaseCommand {
    private final MessageService messages;
    private final GroupSignService signs;

    public GroupSignCommand(MessageService messages, GroupSignService signs) {
        super("sign");

        this.messages = messages;
        this.signs = signs;
    }

    @Override
    public void execute(CommandSender sender, List<String> args) {
        if (args.isEmpty()) {
            messages.send(sender, "Usage.Sign");
            return;
        }

        switch (args.getFirst().toLowerCase(Locale.ROOT)) {
            case "bind" -> handleBind(sender, args);
            case "unbind" -> handleUnbind(sender);
            case "bindat" -> handleBindAt(sender, args);
            case "unbindat" -> handleUnbindAt(sender, args);
            default -> messages.send(sender, "Usage.Sign");
        }
    }

    private void handleBind(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", "console"));
            return;
        }

        if (args.size() < 2) {
            messages.send(sender, "Usage.Sign");
            return;
        }

        final String targetName = args.get(1);
        final Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", targetName));
            return;
        }

        final Block block = player.getTargetBlockExact(6);
        if (!isSign(block)) {
            messages.send(sender, "Errors.LookAtSign");
            return;
        }

        final Location loc = block.getLocation();
        signs.bind(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), target.getUniqueId())
                .thenRun(() -> messages.send(sender, "Sign.Bound", Msg.str("player", target.getName())));
    }

    private void handleUnbind(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", "console"));
            return;
        }

        final Block block = player.getTargetBlockExact(6);
        if (!isSign(block)) {
            messages.send(sender, "Errors.LookAtSign");
            return;
        }

        final Location loc = block.getLocation();
        signs.unbind(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
                .thenAccept(removed -> messages.send(sender, removed ? "Sign.Unbound" : "Sign.NotFound"));
    }

    private void handleBindAt(CommandSender sender, List<String> args) {
        if (args.size() < 6) {
            messages.send(sender, "Usage.SignBindAt");
            return;
        }

        final String playerName = args.get(1);
        final Player target = Bukkit.getPlayerExact(playerName);
        if (target == null) {
            messages.send(sender, "Errors.PlayerNotOnline", Msg.str("player", playerName));
            return;
        }

        final String worldName = args.get(2);
        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            messages.send(sender, "Errors.WorldNotFound", Msg.str("world", worldName));
            return;
        }

        final Integer x = parseIntOrNull(args.get(3));
        final Integer y = parseIntOrNull(args.get(4));
        final Integer z = parseIntOrNull(args.get(5));
        if (x == null || y == null || z == null) {
            messages.send(sender, "Errors.BadNumber", Msg.str("value", args.get(3) + " " + args.get(4) + " " + args.get(5)));
            return;
        }

        final Block block = world.getBlockAt(x, y, z);
        if (!isSign(block)) {
            messages.send(sender, "Errors.LookAtSign");
            return;
        }

        signs.bind(worldName, x, y, z, target.getUniqueId())
                .thenRun(() -> messages.send(sender, "Sign.Bound", Msg.str("player", target.getName())));
    }

    private void handleUnbindAt(CommandSender sender, List<String> args) {
        if (args.size() < 5) {
            messages.send(sender, "Usage.SignUnbindAt");
            return;
        }

        final String worldName = args.get(1);
        final World world = Bukkit.getWorld(worldName);
        if (world == null) {
            messages.send(sender, "Errors.WorldNotFound", Msg.str("world", worldName));
            return;
        }

        final Integer x = parseIntOrNull(args.get(2));
        final Integer y = parseIntOrNull(args.get(3));
        final Integer z = parseIntOrNull(args.get(4));
        if (x == null || y == null || z == null) {
            messages.send(sender, "Errors.BadNumber", Msg.str("value", args.get(2) + " " + args.get(3) + " " + args.get(4)));
            return;
        }

        signs.unbind(worldName, x, y, z)
                .thenAccept(removed -> messages.send(sender, removed ? "Sign.Unbound" : "Sign.NotFound"))
                .exceptionally(ex -> {
                    messages.send(sender, "Sign.Error", Msg.str("error", ex.getMessage() == null ? "unknown" : ex.getMessage()));
                    return null;
                });
    }

    private static Integer parseIntOrNull(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isSign(Block block) {
        if (block == null) return false;
        final Material type = block.getType();
        return block.getState() instanceof Sign ||
                type == Material.OAK_SIGN || type == Material.OAK_WALL_SIGN ||
                type == Material.SPRUCE_SIGN || type == Material.SPRUCE_WALL_SIGN ||
                type == Material.BIRCH_SIGN || type == Material.BIRCH_WALL_SIGN ||
                type == Material.JUNGLE_SIGN || type == Material.JUNGLE_WALL_SIGN ||
                type == Material.ACACIA_SIGN || type == Material.ACACIA_WALL_SIGN ||
                type == Material.DARK_OAK_SIGN || type == Material.DARK_OAK_WALL_SIGN ||
                type == Material.CRIMSON_SIGN || type == Material.CRIMSON_WALL_SIGN ||
                type == Material.WARPED_SIGN || type == Material.WARPED_WALL_SIGN ||
                type == Material.MANGROVE_SIGN || type == Material.MANGROVE_WALL_SIGN ||
                type == Material.BAMBOO_SIGN || type == Material.BAMBOO_WALL_SIGN ||
                type == Material.CHERRY_SIGN || type == Material.CHERRY_WALL_SIGN;
    }

    @Override
    public List<String> tab(CommandSender sender, List<String> args) {
        if (args.isEmpty()) return List.of("bind", "unbind", "bindat", "unbindat");

        if (args.size() == 1) {
            return Stream.of("bind", "unbind", "bindat", "unbindat").filter(s -> s.startsWith(args.getFirst().toLowerCase(Locale.ROOT))).toList();
        }

        if ("bind".equalsIgnoreCase(args.getFirst()) && args.size() == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args.get(1).toLowerCase(Locale.ROOT)))
                    .sorted()
                    .collect(Collectors.toList());
        }

        if ("bindat".equalsIgnoreCase(args.getFirst())) {
            return switch (args.size()) {
                case 2 -> Bukkit.getOnlinePlayers().stream().map(Player::getName).sorted().toList();
                case 3 -> Bukkit.getWorlds().stream().map(World::getName).sorted().toList();
                case 4, 5, 6 -> List.of("<x>", "<y>", "<z>");
                default -> List.of();
            };
        }

        if ("unbindat".equalsIgnoreCase(args.getFirst())) {
            return switch (args.size()) {
                case 2 -> Bukkit.getWorlds().stream().map(World::getName).sorted().toList();
                case 3, 4, 5 -> List.of("<x>", "<y>", "<z>");
                default -> List.of();
            };
        }

        return List.of();
    }
}