package com.github.manu585.manusgroups.util;

import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class GeneralTest {

    @Test
    void allDone_nullOrEmpty_isImmediatelyCompleted() {
        CompletableFuture<Void> f1 = General.allDone(null);
        CompletableFuture<Void> f2 = General.allDone(List.of());

        assertThat(f1).isCompleted();
        assertThat(f2).isCompleted();
    }

    @Test
    void allDone_completesWhenAllComplete() {
        CompletableFuture<Void> a = new CompletableFuture<>();
        CompletableFuture<Void> b = new CompletableFuture<>();
        CompletableFuture<Void> c = new CompletableFuture<>();

        CompletableFuture<Void> all = General.allDone(List.of(a, b, c));
        assertThat(all).isNotDone();

        a.complete(null);
        assertThat(all).isNotDone();

        b.complete(null);
        assertThat(all).isNotDone();

        c.complete(null);
        assertThat(all).isDone().isCompleted();
    }

    @Test
    void all_propagatesFailure() {
        CompletableFuture<Void> a = CompletableFuture.completedFuture(null);
        CompletableFuture<Void> b = new CompletableFuture<>();
        CompletableFuture<Void> c = CompletableFuture.completedFuture(null);

        CompletableFuture<Void> all = General.allDone(new ArrayList<>(List.of(a, b, c)));

        RuntimeException booom = new RuntimeException("Boom");
        b.completeExceptionally(booom);

        assertThatThrownBy(all::join).hasRootCause(booom);
    }
}
