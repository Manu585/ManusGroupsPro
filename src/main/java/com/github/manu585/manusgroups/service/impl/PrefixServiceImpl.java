package com.github.manu585.manusgroups.service.impl;

import com.github.manu585.manusgroups.cache.GroupPlayerCache;
import com.github.manu585.manusgroups.service.spi.PrefixService;
import com.github.manu585.manusgroups.util.General;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PrefixServiceImpl implements PrefixService {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private final ConcurrentHashMap<UUID, Component> cache = new ConcurrentHashMap<>();

    private final JavaPlugin plugin;
    private final GroupPlayerCache players;

    public PrefixServiceImpl(final JavaPlugin plugin, final GroupPlayerCache players) {
        this.plugin = plugin;
        this.players = players;
    }

    @Override
    public Component cachedPrefix(UUID uuid) {
        return cache.getOrDefault(uuid, Component.empty());
    }

    @Override
    public CompletableFuture<Component> primePrefix(UUID uuid) {
        return players.getOrLoad(uuid).thenApply(groupPlayer -> {
            Component component = (groupPlayer.getPrimaryGroup() == null) ? Component.empty() : MM.deserialize(groupPlayer.getPrimaryGroup().prefix());
            cache.put(uuid, component);

            return component;
        });
    }

    @Override
    public void invalidate(UUID uuid) {
        cache.remove(uuid);
    }

    @Override
    public void refreshDisplayName(Player player) {
        UUID uuid = player.getUniqueId();
        Component cached = cachedPrefix(uuid);

        if (!cached.equals(Component.empty())) {
            General.runSync(plugin, () -> applyDisplayName(player, cached));
            return;
        }

        primePrefix(uuid).thenAccept(prefix -> General.runSync(plugin, () -> applyDisplayName(player, prefix)));
    }

    private void applyDisplayName(Player player, Component prefix) {
        Component display = Component.empty()
                .append(prefix)
                .append(Component.space())
                .append(player.name());

        player.displayName(display);
        player.playerListName(display);
        player.customName(display);

        applyScoreboardTeamPrefix(player, prefix);
    }

    private void applyScoreboardTeamPrefix(Player player, Component prefix) {
        Scoreboard scoreboard = player.getScoreboard();
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();

        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = scoreboardManager.getMainScoreboard();
        }

        String teamName = teamNameFor(player.getUniqueId());
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.prefix(prefix.append(Component.space()));

        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
    }

    public static String teamNameFor(UUID uuid) {
        return "mgp_" + uuid.toString().substring(0, 12);
    }
}
