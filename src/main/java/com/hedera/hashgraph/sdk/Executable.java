package com.hedera.hashgraph.sdk;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import org.threeten.bp.Duration;

public abstract class Executable<O> {
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    Executable() {}

    public abstract CompletableFuture<O> executeAsync(Client client);

    public void executeAsync(Client client, BiConsumer<O, Throwable> callback) {
        executeAsync(client, DEFAULT_TIMEOUT, callback);
    }

    @SuppressWarnings("InconsistentOverloads")
    public void executeAsync(Client client, Duration timeout, BiConsumer<O, Throwable> callback) {
        executeAsync(client)
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete(callback);
    }

    public O execute(Client client) throws TimeoutException {
        return execute(client, DEFAULT_TIMEOUT);
    }

    public O execute(Client client, Duration timeout) throws TimeoutException {
        try {
            return executeAsync(client).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            var cause = e.getCause();

            // If there is no cause, just re-throw
            if (cause == null) throw new RuntimeException(e);

            // TODO: For explicit errors we want to have as checked, we need to
            //       do instanceof checks and bridge that here

            // Unwrap and re-wrap as a RuntimeException
            throw new RuntimeException(cause);
        }
    }
}
