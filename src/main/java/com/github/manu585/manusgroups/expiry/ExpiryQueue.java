package com.github.manu585.manusgroups.expiry;

import com.github.manu585.manusgroups.domain.GroupAssignment;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ExpiryQueue implements ExpiryScheduler {
    private final DelayQueue<Item> queue = new DelayQueue<>();
    private final ConcurrentHashMap<UUID, Item> index = new ConcurrentHashMap<>();

    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "groups-pro-expiry");
        thread.setDaemon(true);
        return thread;
    });

    private volatile boolean running = false;
    private volatile Consumer<UUID> onExpire;

    /**
     * Register the callback that performs the domain (GroupService) action
     */
    @Override
    public void registerListener(Consumer<UUID> onExpire) {
        this.onExpire = onExpire;
    }

    /**
     * Start background worker thread
     */
    @Override
    public void start() {
        if (running) return;
        running = true;
        worker.submit(this::loop);
    }

    /**
     * Stop worker thread and clear states
     */
    @Override
    public void stop() {
        running = false;
        worker.shutdownNow();
        queue.clear();
        index.clear();
    }

    /**
     * Schedule or cancel a users timer
     *
     * @param user UUID of user
     * @param expiresAt When it should fire
     */
    @Override
    public void scheduleOrCancel(UUID user, Instant expiresAt) {
        if (expiresAt == null) {
            remove(user);
            return;
        }

        Item fresh = new Item(user, expiresAt);
        Item old = index.put(user, fresh);
        if (old != null) {
            queue.remove(old); // remove stale tasks if present
        }

        queue.offer(fresh);
    }

    /**
     * Already expired -> trigger callback async now <br>
     * Future expiry -> schedule normally
     */
    @Override
    public void bootstrap(List<GroupAssignment> groupAssignments) {
        for (GroupAssignment assignment : groupAssignments) {
            if (assignment.expiresAt() == null) continue; // Not expireable, skip
            if (!assignment.expiresAt().isAfter(Instant.now())) {
                // expired
                final Consumer<UUID> listener = this.onExpire;
                if (listener != null) {
                    CompletableFuture.runAsync(() -> listener.accept(assignment.uuid()));
                }
            } else {
                scheduleOrCancel(assignment.uuid(), assignment.expiresAt());
            }
        }
    }

    public void remove(UUID user) {
        final Item old = index.remove(user);
        if (old != null) {
            queue.remove(old);
        }
    }

    /**
     * Worker loop, blocks on the next due item, verifies it's still current for the user,
     * invokes listener and removes it from the index
     */
    private void loop() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                final Item due = queue.take(); // block until due

                // Users timer been replaced since queued?
                if (index.getOrDefault(due.user, due) != due) continue;

                final Consumer<UUID> listener = this.onExpire;
                if (listener != null) {
                    listener.accept(due.user);
                }
                remove(due.user);
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class Item implements Delayed {
        final UUID user;
        final long epochMilli;

        Item(UUID user, Instant at) {
            this.user = user;
            this.epochMilli = at.toEpochMilli();
        }

        @Override
        public long getDelay(TimeUnit unit) {
            final long diff = epochMilli - System.currentTimeMillis();
            return unit.convert(diff, TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(@NotNull Delayed delayed) {
            return Long.compare(this.epochMilli, ((Item) delayed).epochMilli);
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Item i) && i.user.equals(this.user);
        }
    }
}
