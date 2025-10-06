package com.github.manu585.manusgroups.service.impl;

import com.github.manu585.manusgroups.cache.GroupPlayerCache;
import com.github.manu585.manusgroups.cache.PrefixCache;
import com.github.manu585.manusgroups.service.MessageService;
import com.github.manu585.manusgroups.service.spi.PrefixService;
import com.github.manu585.manusgroups.util.General;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PrefixServiceImpl implements PrefixService {
    private final JavaPlugin plugin;
    private final GroupPlayerCache players;
    private final PrefixCache prefixCache;
    private final MessageService messages;

    public PrefixServiceImpl(final JavaPlugin plugin, final GroupPlayerCache players, final PrefixCache prefixCache, final MessageService messages) {
        this.plugin = plugin;
        this.players = players;
        this.prefixCache = prefixCache;
        this.messages = messages;
    }

    @Override
    public Component cachedPrefix(UUID uuid) {
        return prefixCache.getOrDefault(uuid);
    }

    @Override
    public CompletableFuture<Component> primePrefix(UUID uuid) {
        return players.getOrLoad(uuid).thenApply(groupPlayer -> {
            final Component component = (groupPlayer.primaryGroup() == null) ? Component.empty() : messages.mm().deserialize(groupPlayer.primaryGroup().prefix());
            prefixCache.put(uuid, component);

            return component;
        });
    }

    @Override
    public void invalidate(UUID uuid) {
        prefixCache.invalidate(uuid);
    }

    @Override
    public void refreshDisplayName(Player player) {
        final UUID uuid = player.getUniqueId();
        final Component cached = cachedPrefix(uuid);

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
        final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();

        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            scoreboard = scoreboardManager.getMainScoreboard();
        }

        final String teamName = teamNameFor(player.getUniqueId());
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
