package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import io.grpc.ChannelCredentials;
import io.grpc.ConnectivityState;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import io.grpc.inprocess.InProcessChannelBuilder;

import javax.annotation.Nullable;
import java8.util.concurrent.CompletableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

abstract class ManagedNode {
    private static final String IN_PROCESS = "in-process:";
    private static final int GET_STATE_INTERVAL_MILLIS = 50;
    private static final int GET_STATE_TIMEOUT_MILLIS = 10000;
    private static final int GET_STATE_MAX_ATTEMPTS = GET_STATE_TIMEOUT_MILLIS / GET_STATE_INTERVAL_MILLIS;
    private boolean hasConnected = false;
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

    boolean channelFailedToConnect() {
        if (hasConnected) {
            return false;
        }
        @Var var state = getChannel().getState(true);
        try {
            for (@Var int i = 0; i < GET_STATE_MAX_ATTEMPTS && state != ConnectivityState.READY; i++) {
                TimeUnit.MILLISECONDS.sleep(GET_STATE_INTERVAL_MILLIS);
                state = getChannel().getState(true);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        hasConnected = (state == ConnectivityState.READY);
        return !hasConnected;
    }

    private CompletableFuture<Boolean> channelFailedToConnectAsync(int i, ConnectivityState state) {
        hasConnected = (state == ConnectivityState.READY);
        if (i >= GET_STATE_MAX_ATTEMPTS || hasConnected) {
            return CompletableFuture.completedFuture(!hasConnected);
        }
        return Delayer.delayFor(GET_STATE_INTERVAL_MILLIS, executor).thenCompose(ignored -> {
            return channelFailedToConnectAsync(i + 1, getChannel().getState(true));
        });
    }

    CompletableFuture<Boolean> channelFailedToConnectAsync() {
        if (hasConnected) {
            return CompletableFuture.completedFuture(false);
        }
        return channelFailedToConnectAsync(0, getChannel().getState(true));
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
