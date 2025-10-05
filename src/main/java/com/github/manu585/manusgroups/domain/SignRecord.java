package com.github.manu585.manusgroups.domain;

import java.util.UUID;

public record SignRecord(String world, int x, int y, int z, UUID target) {}
