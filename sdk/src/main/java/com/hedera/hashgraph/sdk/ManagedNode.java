package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import io.grpc.ChannelCredentials;
import io.grpc.ConnectivityState;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.TlsChannelCredentials;
import io.grpc.inprocess.InProcessChannelBuilder;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

abstract class ManagedNode {
    private static final String IN_PROCESS = "in-process:";
    private static final int GET_STATE_INTERVAL_MILLIS = 250;
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

        var channel = channelBuilder
            .keepAliveTimeout(10, TimeUnit.SECONDS)
            .userAgent(getUserAgent())
            .executor(executor)
            .build();

        @Var var state = channel.getState(true);
        try {
            for (@Var int i = 0; i < (10000 / GET_STATE_INTERVAL_MILLIS) && state != ConnectivityState.READY; i++) {
                TimeUnit.MILLISECONDS.sleep(GET_STATE_INTERVAL_MILLIS);
                state = channel.getState(true);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (state != ConnectivityState.READY) {
            System.out.println("not ready");
            channel.shutdown();

            try {
                channel.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            throw new StatusRuntimeException(Status.UNAVAILABLE);
        }

        this.channel = channel;
        return this.channel;
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
