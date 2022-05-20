package com.hedera.hashgraph.sdk;

import java8.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
/**
 * Utility class used internally by the sdk.
 */
final class Delayer {
    private static final Logger logger = LoggerFactory.getLogger(Delayer.class);

    private static final Duration MIN_DELAY = Duration.ofMillis(500);

    private Delayer() {
    }

    static CompletableFuture<Void> delayBackOff(int attempt, Executor executor) {
        var interval = MIN_DELAY.multipliedBy(ThreadLocalRandom.current().nextLong(1L << attempt));

        return delayFor(interval.toMillis(), executor);
    }

    static CompletableFuture<Void> delayFor(long milliseconds, Executor executor) {
        logger.trace("waiting for {} seconds before trying again", (double) milliseconds / 1000.0);

        return CompletableFuture.runAsync(
            () -> {
            },
            CompletableFuture.delayedExecutor(milliseconds, TimeUnit.MILLISECONDS, executor));
    }
}
