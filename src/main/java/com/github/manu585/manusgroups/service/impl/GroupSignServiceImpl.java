package com.github.manu585.manusgroups.service.impl;

import com.github.manu585.manusgroups.cache.GroupPlayerCache;
import com.github.manu585.manusgroups.domain.Group;
import com.github.manu585.manusgroups.domain.GroupPlayer;
import com.github.manu585.manusgroups.domain.SignRecord;
import com.github.manu585.manusgroups.repo.GroupRepository;
import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.spi.GroupSignService;
import com.github.manu585.manusgroups.service.util.Msg;
import com.github.manu585.manusgroups.util.DefaultGroup;
import com.github.manu585.manusgroups.util.General;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class GroupSignServiceImpl implements GroupSignService {
    private final JavaPlugin plugin;
    private final GroupRepository repository;
    private final GroupPlayerCache players;
    private final MessageService messages;

    public GroupSignServiceImpl(JavaPlugin plugin, GroupRepository repository, GroupPlayerCache players, MessageService messages) {
        this.plugin = plugin;
        this.repository = repository;
        this.players = players;
        this.messages = messages;
    }

    @Override
    public CompletableFuture<Void> bind(String world, int x, int y, int z, UUID target) {
        final World w = plugin.getServer().getWorld(world);
        if (w == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Ensure a sign block exists at the location (main thread), then upsert and render
        return ensureSignAt(w, x, y, z)
                .thenCompose(__ -> repository.upsertSign(world, x, y, z, target))
                .thenCompose(__ -> renderAt(w, x, y, z, target));
    }

    @Override
    public CompletableFuture<Boolean> unbind(String world, int x, int y, int z) {
        final World w = plugin.getServer().getWorld(world);
        return repository.deleteSignAt(world, x, y, z).thenCompose(removed -> {
                    if (!removed || w == null) {
                        return CompletableFuture.completedFuture(removed);
                    }

                    return clearTextAt(w, x, y, z).thenApply(__ -> true);
                });
    }

    @Override
    public void refreshFor(UUID target) {
        repository.listSignsByTarget(target).thenCompose(list -> {
            final List<CompletableFuture<Void>> tasks = new ArrayList<>();

            for (SignRecord signRecord : list) {
                final World world = plugin.getServer().getWorld(signRecord.world());
                if (world == null) continue;
                tasks.add(renderAt(world, signRecord.x(), signRecord.y(), signRecord.z(), signRecord.target()));
            }

            return General.allDone(tasks);
        });
    }

    private CompletableFuture<Void> renderAt(World world, int x, int y, int z, UUID target) {
        if (world == null) {
            return CompletableFuture.completedFuture(null);
        }

        return players.getOrLoad(target).thenCompose(groupPlayer -> General.runMain(plugin, (() -> applyToBlock(world, x, y, z, groupPlayer))));
    }

    private void applyToBlock(World world, int x, int y, int z, GroupPlayer groupPlayer) {
        final Block block = world.getBlockAt(x, y, z);
        if (!(block.getState() instanceof Sign sign)) return;

        final String playerName = findName(groupPlayer.uuid());
        final Group group = groupPlayer.primaryGroup();
        final String groupName = (group == null) ? DefaultGroup.name() : group.name();
        final Component prefix = (group == null) ? messages.mm().deserialize(DefaultGroup.group().prefix()) : messages.mm().deserialize(group.prefix());

        Component l1 = messages.formatToComponent("Signs.Format.Line1", Msg.comp("prefix", prefix), Msg.str("player", playerName), Msg.str("group", groupName));
        Component l2 = messages.formatToComponent("Signs.Format.Line2", Msg.comp("prefix", prefix), Msg.str("player", playerName), Msg.str("group", groupName));
        Component l3 = messages.formatToComponent("Signs.Format.Line3", Msg.comp("prefix", prefix), Msg.str("player", playerName), Msg.str("group", groupName));
        Component l4 = messages.formatToComponent("Signs.Format.Line4", Msg.comp("prefix", prefix), Msg.str("player", playerName), Msg.str("group", groupName));

        sign.getSide(Side.FRONT).line(0, l1);
        sign.getSide(Side.FRONT).line(1, l2);
        sign.getSide(Side.FRONT).line(2, l3);
        sign.getSide(Side.FRONT).line(3, l4);
        sign.update(true, false);
    }

    private CompletableFuture<Void> ensureSignAt(World world, int x, int y, int z) {
        return General.runMain(plugin, (() -> {
            Block block = world.getBlockAt(x, y, z);
            if (!(block.getState() instanceof Sign)) {
                block.setType(Material.OAK_SIGN, false);
            }
        }));
    }

    private CompletableFuture<Void> clearTextAt(World world, int x, int y, int z) {
        return General.runMain(plugin, (() -> {
            Block block = world.getBlockAt(x, y, z);
            if (block.getState() instanceof Sign sign) {
                for (int i = 0; i < 4; i++) {
                    sign.getSide(Side.FRONT).line(i, Component.empty());
                }
                sign.update(true, false);
            }
        }));
    }

    private String findName(UUID uuid) {
        final Player player = plugin.getServer().getPlayer(uuid);
        return (player != null) ? player.getName() : uuid.toString();
    }
}
