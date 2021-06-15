package com.hedera.hashgraph.sdk;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

abstract class ManagedNode {
    String address;
    @Nullable
    ManagedChannel channel = null;
    final ExecutorService executor;
    long lastUsed = 0;
    long useCount = 0;

    ManagedNode(String address, ExecutorService executor) {
        this.executor = executor;
        this.address = address;
    }

    synchronized void inUse() {
        useCount++;
        lastUsed = System.currentTimeMillis();
    }

    synchronized ManagedChannel getChannel() {
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

    synchronized void close(long seconds) throws InterruptedException {
        if (channel != null) {
            channel.shutdown();
            channel.awaitTermination(seconds, TimeUnit.SECONDS);
            channel = null;
        }
    }

    private String getUserAgent() {
        var thePackage = getClass().getPackage();
        var implementationVersion = thePackage != null ? thePackage.getImplementationVersion() : null;
        return "hedera-sdk-java/" + ((implementationVersion != null) ? ("v" + implementationVersion) : "DEV");
    }
}
