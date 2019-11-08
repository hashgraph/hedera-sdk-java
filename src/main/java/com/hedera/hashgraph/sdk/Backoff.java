package com.hedera.hashgraph.sdk;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class Backoff {
    private int attempt = 0;

    private final Duration baseDelay;
    private final Instant expiration;

    Backoff(Duration baseDelay, Instant expiration) {
        this.baseDelay = baseDelay;
        this.expiration = expiration;
    }

    private Optional<Duration> getNextDelay() {
        attempt += 1;

        final Duration nextDelay = baseDelay.multipliedBy(
            ThreadLocalRandom.current().nextLong(1L << attempt));

        if (Instant.now().plus(nextDelay).isBefore(expiration)) {
            return Optional.of(nextDelay);
        } else {
            return Optional.empty();
        }
    }

    <T, E extends Exception> T tryWhile(Predicate<E> shouldRetry, FallibleProducer<T, E> producer) throws E {
        for (;;) {
            try {
                return producer.tryProduce();
            } catch (Throwable e) {
                final Optional<Duration> nextDelay = getNextDelay();

                // `producer` can only throw `E` as a checked exception
                //noinspection unchecked
                if (shouldRetry.test((E) e) && nextDelay.isPresent()) {
                    ThreadUtil.sleepDuration(nextDelay.get());
                } else {
                    throw e;
                }
            }
        }
    }

    <T, E> void asyncTryWhile(Predicate<E> shouldRetry, Consumer<Consumer<E>> onTry, Consumer<E> onError) {
        onTry.accept(e -> {
            final Optional<Duration> nextDelay = getNextDelay();

            if (shouldRetry.test(e) && nextDelay.isPresent()) {
                ThreadUtil.schedule(() ->
                        asyncTryWhile(shouldRetry, onTry, onError),
                    nextDelay.get());
            } else {
                onError.accept(e);
            }
        });
    }

    @FunctionalInterface
    interface FallibleProducer<T, E extends Exception> {
        T tryProduce() throws E;
    }
}
