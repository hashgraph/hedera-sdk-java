package com.hedera.hashgraph.sdk;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

abstract class ManagedNode {
    String address;
    ManagedChannel channel;
    final ExecutorService executor;
    Long lastUsed = null;

    ManagedNode(String address, ExecutorService executor) {
        this.executor = executor;
        this.address = address;
    }

    synchronized ManagedChannel getChannel() {
        this.lastUsed = Instant.now().toEpochMilli();

        if (channel != null) {
            return channel;
        }

        channel = ManagedChannelBuilder.forTarget(address)
            .usePlaintext()
            .userAgent(getUserAgent())
            .executor(executor)
            .build();

        return channel;
    }

    void close() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private String getUserAgent() {
        var thePackage = getClass().getPackage();
        var implementationVersion = thePackage != null ? thePackage.getImplementationVersion() : null;
        return "hedera-sdk-java/" + ((implementationVersion != null) ? ("v" + implementationVersion) : "DEV");
    }
}
