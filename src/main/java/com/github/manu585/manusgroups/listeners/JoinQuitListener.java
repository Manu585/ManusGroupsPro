package com.github.manu585.manusgroups.listeners;

import com.github.manu585.manusgroups.service.spi.PermissionService;
import com.github.manu585.manusgroups.service.GroupService;
import com.github.manu585.manusgroups.service.spi.PrefixService;
import com.github.manu585.manusgroups.util.General;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import static com.github.manu585.manusgroups.service.impl.PrefixServiceImpl.teamNameFor;

public class JoinQuitListener implements Listener {
    private final JavaPlugin plugin;
    private final GroupService groupService;
    private final PrefixService prefixService;
    private final PermissionService permissionService;

    public JoinQuitListener(final JavaPlugin plugin, final GroupService groupService, final PrefixService prefixService, final PermissionService permissionService) {
        this.plugin = plugin;
        this.groupService = groupService;
        this.prefixService = prefixService;
        this.permissionService = permissionService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Make sure default group exists
        groupService.ensureDefaultPersisted(event.getPlayer().getUniqueId())
                // Load groupPlayer (snapshot)
                .thenCompose(__ -> groupService.load(event.getPlayer().getUniqueId()))
                // Prime prefix cache
                .thenCompose(__ -> prefixService.primePrefix(event.getPlayer().getUniqueId()))
                // Permissions applien
                .thenCompose(__ -> permissionService.refreshFor(event.getPlayer().getUniqueId()))
                // Refresh display name
                .thenRun(() -> General.runSync(plugin, () -> prefixService.refreshDisplayName(event.getPlayer())
                ));

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Scoreboard scoreboard = event.getPlayer().getScoreboard();
        Team team = scoreboard.getTeam(teamNameFor(event.getPlayer().getUniqueId()));
        if (team != null) {
            team.unregister();
        }

        groupService.invalidateCachesFor(event.getPlayer().getUniqueId());
    }
}
