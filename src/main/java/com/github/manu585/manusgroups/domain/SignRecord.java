package com.github.manu585.manusgroups.domain;

import org.bukkit.World;

import java.util.UUID;

public record SignRecord(String world, int x, int y, int z, UUID target) {
    public boolean matches(World w, int X, int Y, int Z) {
        return w.getName().equalsIgnoreCase(world) && x == X && y == Y && z == Z;
    }
}
