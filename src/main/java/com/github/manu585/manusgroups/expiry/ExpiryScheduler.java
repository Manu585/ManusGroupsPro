package com.github.manu585.manusgroups.expiry;

import com.github.manu585.manusgroups.domain.GroupAssignment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface ExpiryScheduler {
    void start();
    void stop();

    void scheduleOrCancel(UUID user, Instant expiresAt);
    void bootstrap(List<GroupAssignment> groupAssignmentList);
    void registerListener(Consumer<UUID> onExpire);
}
