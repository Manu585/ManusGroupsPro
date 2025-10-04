package com.github.manu585.manusgroups.spi;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PrefixService {
    /**
     * @return Cached prefix or {@link Component#empty()} if not ready yet.
     */
    Component cachedPrefix(UUID uuid);

    /**
     * Computes prefix from players groups async and updates the prefix cache
     */
    CompletableFuture<Component> primePrefix(UUID uuid);

    /**
     * Clears the cached prefix
     */
    void invalidate(UUID uuid);

    /**
     * Refreshes the DisplayName of a player
     * @param player Player who needs it refreshed
     */
    void refreshDisplayName(Player player);
}
