package com.github.manu585.manusgroups.listeners;

import com.github.manu585.manusgroups.events.GroupChangeEvent;
import com.github.manu585.manusgroups.service.spi.PrefixService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GroupChangeListener implements Listener {
    private final PrefixService prefixService;

    public GroupChangeListener(final PrefixService prefixService) {
        this.prefixService = prefixService;
    }

    @EventHandler
    public void onGroupChange(GroupChangeEvent event) {
        final Player player = Bukkit.getPlayer(event.getUuid());
        if (player == null || !player.isOnline()) return;

        prefixService.invalidate(event.getUuid());
        prefixService.primePrefix(event.getUuid()).thenAccept(__ -> prefixService.refreshDisplayName(player));
    }
}
