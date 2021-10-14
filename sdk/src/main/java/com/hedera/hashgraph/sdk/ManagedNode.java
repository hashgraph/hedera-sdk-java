package com.hedera.hashgraph.sdk;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import io.grpc.inprocess.InProcessChannelBuilder;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

abstract class ManagedNode {
    private static final String IN_PROCESS = "in-process:";
    final ExecutorService executor;
    String address;
    long lastUsed = 0;
    long useCount = 0;

    @Nullable
    ManagedChannel channel = null;

    ManagedNode(String address, ExecutorService executor) {
        this.executor = executor;
        this.address = address;
    }

    synchronized void inUse() {
        useCount++;
        lastUsed = System.currentTimeMillis();
    }

    ChannelCredentials getChannelCredentials() {
        return TlsChannelCredentials.create();
    }

    synchronized ManagedChannel getChannel() {
        if (channel != null) {
            return channel;
        }

        ManagedChannelBuilder<?> channelBuilder;

        if (address.startsWith(IN_PROCESS)) {
          String name = address.substring(IN_PROCESS.length());
          channelBuilder = InProcessChannelBuilder.forName(name);
        } else if (address.endsWith(":50212") || address.endsWith(":443")) {
            channelBuilder = Grpc.newChannelBuilder(address, getChannelCredentials()).overrideAuthority("127.0.0.1");
        } else {
            channelBuilder = ManagedChannelBuilder.forTarget(address).usePlaintext();
        }

        channel = channelBuilder
            .keepAliveTimeout(10, TimeUnit.SECONDS)
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
