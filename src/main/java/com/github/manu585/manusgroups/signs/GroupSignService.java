package com.github.manu585.manusgroups.signs;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GroupSignService {
    /**
     * Bind (create or update) a sign at the given location to show info for target player
     * @param world What world
     * @param x x Coordinate
     * @param y y Coordinate
     * @param z z Coordinate
     * @param target UUID of target
     */
    CompletableFuture<Void> bind(String world, int x, int y, int z, UUID target);

    /**
     * Remove sign binding at a given location
     * @param world What world
     * @param x x Coordinate
     * @param y Y Coordinate
     * @param z Z Coordinate
     */
    CompletableFuture<Boolean> unbind(String world, int x, int y, int z);

    /**
     * Refresh all signs that display this players info
     *
     * @param target UUID of player
     */
    void refreshFor(UUID target);


    /**
     * Re-render all signs
     */
    CompletableFuture<Void> refreshAll();
}
