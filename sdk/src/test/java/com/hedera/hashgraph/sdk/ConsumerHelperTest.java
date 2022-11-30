package com.hedera.hashgraph.sdk;

import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ConsumerHelperTest {
    @Test
    void biConsumer() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
        BiConsumer<String, Throwable> consumer = mock(BiConsumer.class);
        ConsumerHelper.biConsumer(future, consumer);
        future.join();
        verify(consumer, times(1)).accept(any(), any());
    }

    @Test
    void twoConsumersWithoutError() {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
        Consumer<String> onSuccess = mock(Consumer.class);
        Consumer<Throwable> onFailure = mock(Consumer.class);
        ConsumerHelper.twoConsumers(future, onSuccess, onFailure);
        future.join();
        verify(onSuccess, times(1)).accept("Hello");
        verify(onFailure, times(0)).accept(any());
    }
    @Test
    void twoConsumersWithError() {
        CompletableFuture<String> future = CompletableFuture.failedFuture(new RuntimeException("Exception"));
        Consumer<String> onSuccess = mock(Consumer.class);
        Consumer<Throwable> onFailure = mock(Consumer.class);
        ConsumerHelper.twoConsumers(future, onSuccess, onFailure);
        assertThrows(RuntimeException.class, future::join);
        verify(onSuccess, times(0)).accept(any());
        verify(onFailure, times(1)).accept(any());
    }
}
