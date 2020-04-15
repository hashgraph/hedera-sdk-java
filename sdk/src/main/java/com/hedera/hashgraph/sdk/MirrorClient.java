package com.hedera.hashgraph.sdk;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public final class MirrorClient implements AutoCloseable {
    final ManagedChannel channel;

    public MirrorClient(String endpoint) {
        channel = ManagedChannelBuilder.forTarget(endpoint)
            .keepAliveTime(2, TimeUnit.MINUTES)
            .usePlaintext()
            .build();
    }

    @Override
    public void close() throws InterruptedException {
        close(30, TimeUnit.SECONDS);
    }

    public boolean close(long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        // shutdownNow() is required because we have by-design infinitely running calls
        channel.shutdownNow();
        return channel.awaitTermination(timeout, timeoutUnit);
    }
}
