package com.hedera.hashgraph.sdk;


import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;


class ConsumerHelper {
    static <T> void biConsumer(CompletableFuture<T> future, BiConsumer<T, Throwable> consumer) {
        future.whenComplete(consumer);
    }

    static <T> void twoConsumers(CompletableFuture<T> future, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        future.whenComplete((output, error) -> {
            if (error != null) {
                onFailure.accept(error);
            } else {
                onSuccess.accept(output);
            }
        });
    }
}
