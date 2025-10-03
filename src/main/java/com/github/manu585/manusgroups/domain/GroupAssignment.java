package com.github.manu585.manusgroups.domain;

import java.time.Instant;
import java.util.UUID;

public record GroupAssignment(UUID uuid, String groupName, Instant expiresAt) {}
