package com.hedera.hashgraph.sdk;

import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class ConsumerHelperTest {

    @Test
    void biConsumer() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
        BiConsumer<String, Throwable> consumer = mock(BiConsumer.class);
        ConsumerHelper.biConsumer(future, consumer);
        verify(consumer, times(1)).accept(any(), any());
    }

    @Test
    void twoConsumersWithoutError() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
        Consumer<String> onSuccess = mock(Consumer.class);
        Consumer<Throwable> onFailure = mock(Consumer.class);
        ConsumerHelper.twoConsumers(future, onSuccess, onFailure);
        verify(onSuccess, times(1)).accept(any());
        verify(onFailure, times(0)).accept(any());
    }

    @Test
    void twoConsumersWithError() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Exception");
        });
        Consumer<String> onSuccess = mock(Consumer.class);
        Consumer<Throwable> onFailure = mock(Consumer.class);
        ConsumerHelper.twoConsumers(future, onSuccess, onFailure);
        verify(onSuccess, times(0)).accept(any());
        verify(onFailure, times(1)).accept(any());
    }
}
