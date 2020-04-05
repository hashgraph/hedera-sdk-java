package com.hedera.hashgraph.sdk;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java8.util.concurrent.CompletableFuture;
import org.threeten.bp.Duration;

class Delayer {
    private static final Duration MIN_DELAY = Duration.ofMillis(500);

    private Delayer() {}

    static CompletableFuture<Void> delayBackOff(int attempt, Executor executor) {
        var interval = MIN_DELAY.multipliedBy(ThreadLocalRandom.current().nextLong(1L << attempt));
        return delayFor(interval.toMillis(), executor);
    }

    static CompletableFuture<Void> delayFor(long milliseconds, Executor executor) {
        return CompletableFuture.runAsync(
                () -> {},
                CompletableFuture.delayedExecutor(milliseconds, TimeUnit.MILLISECONDS, executor));
    }
}
