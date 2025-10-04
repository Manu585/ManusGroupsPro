package com.github.manu585.manusgroups.repo;

import java.util.concurrent.*;

public final class DbExecutor {
    private final ExecutorService service = Executors.newFixedThreadPool(4, r -> {
        Thread thread = new Thread(r, "groups-pro");
        thread.setDaemon(true);
        return thread;
    });

    public <T>CompletableFuture<T> supply(final SqlSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception exception) {
                throw new CompletionException(exception);
            }
        }, service);
    }

    public CompletableFuture<Void> run(final SqlRunnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception exception) {
                throw new CompletionException(exception);
            }
        }, service);
    }

    @FunctionalInterface
    public interface SqlSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface SqlRunnable {
        void run() throws Exception;
    }

    public void shutDown() {
        service.shutdown();

        try {
            if (!service.awaitTermination(3, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException ignored) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
