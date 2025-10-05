package com.github.manu585.manusgroups.permissions;

import com.github.manu585.manusgroups.cache.GroupPermissionCache;
import com.github.manu585.manusgroups.cache.GroupPlayerCache;
import com.github.manu585.manusgroups.domain.GroupPlayer;
import com.github.manu585.manusgroups.util.General;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionServiceImpl implements PermissionService {
    private final ConcurrentHashMap<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();

    private final JavaPlugin plugin;
    private final GroupPermissionCache permissionCache;
    private final GroupPlayerCache playerCache;

    public PermissionServiceImpl(final JavaPlugin plugin, final GroupPermissionCache permissionCache, final GroupPlayerCache playerCache) {
        this.plugin = plugin;
        this.permissionCache = permissionCache;
        this.playerCache = playerCache;
    }

    @Override
    public CompletableFuture<Void> applyFor(UUID user, String groupName) {
        return permissionCache.getOrLoad(groupName).thenCompose(nodes -> runMain(() -> applyNow(user, nodes)));
    }

    @Override
    public CompletableFuture<Void> clear(UUID user) {
        return runMain(() -> {
            PermissionAttachment attachment = attachments.remove(user);
            Player player = plugin.getServer().getPlayer(user);

            if (attachment != null && player != null && player.isOnline()) {
                player.removeAttachment(attachment);
                player.recalculatePermissions();
            }
        });
    }

    @Override
    public CompletableFuture<Void> refreshAllForGroup(String groupName) {
        List<CompletableFuture<Void>> tasks = new ArrayList<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            GroupPlayer groupPlayer = playerCache.getIfPresent(player.getUniqueId());
            if (groupPlayer == null) continue;
            if (!groupPlayer.hasGroup(groupName)) continue;

            tasks.add(applyFor(player.getUniqueId(), groupName));
        }

        return tasks.isEmpty() ? CompletableFuture.completedFuture(null) : CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
    }

    /**
     * Resolve the users primary group (async) and apply that group permissions
     */
    @Override
    public CompletableFuture<Void> refreshFor(UUID user) {
        return playerCache.getOrLoad(user).thenCompose(groupPlayer -> {
            if (groupPlayer.getPrimaryGroup() == null) {
                return clear(user);
            }

            final String groupName = groupPlayer.getPrimaryGroup().name();
            return applyFor(user, groupName);
        });
    }

    private void applyNow(UUID user, Map<String, Boolean> rawNodes) {
        Player player = plugin.getServer().getPlayer(user);
        if (player == null || !player.isOnline()) {
            attachments.remove(user);
            return;
        }

        final List<String> registered = new ArrayList<>();

        for (final Permission permission : plugin.getServer().getPluginManager().getPermissions()) {
            registered.add(permission.getName());
        }

        final Map<String, Boolean> nodes = PermissionExpander.expand(rawNodes, registered);
        final PermissionAttachment attachment = attachments.computeIfAbsent(user, __ -> player.addAttachment(plugin));

        // Clear old permissions
        for (String old : new ArrayList<>(attachment.getPermissions().keySet())) {
            attachment.unsetPermission(old);
        }

        // Apply new ones
        nodes.forEach(attachment::setPermission);

        player.recalculatePermissions();
        player.updateCommands();
    }

    private CompletableFuture<Void> runMain(Runnable r) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        General.runSync(plugin, () -> {
            try {
                r.run();
                future.complete(null);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }
}
