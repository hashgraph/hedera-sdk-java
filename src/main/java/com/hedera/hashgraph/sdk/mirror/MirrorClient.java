package com.hedera.hashgraph.sdk.mirror;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MirrorClient implements AutoCloseable {
    final ManagedChannel channel;

    public MirrorClient(String endpoint) {
        channel = ManagedChannelBuilder.forTarget(endpoint)
            .keepAliveTime(2, TimeUnit.MINUTES)
            .usePlaintext()
            .build();
    }

    @Override
    public void close() throws InterruptedException {
        close(5, TimeUnit.SECONDS);
    }

    public boolean close(long timeout, TimeUnit timeoutUnit) throws InterruptedException {
        // shutdownNow() is required because we have by-design infinitely running calls
        channel.shutdownNow();
        return channel.awaitTermination(timeout, timeoutUnit);
    }
}
