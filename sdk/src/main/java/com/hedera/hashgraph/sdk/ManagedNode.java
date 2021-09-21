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
    final ExecutorService executor;
    ManagedNodeAddress address;
    long lastUsed = 0;
    long useCount = 0;

    @Nullable
    ManagedChannel channel = null;
    boolean transportSecurityChanged;

    ManagedNode(String address, ExecutorService executor) {
        this.executor = executor;
        this.address = ManagedNodeAddress.fromString(address);
    }

    ManagedNode setTransportSecurity(boolean transportSecurity) {
        address.setTransportSecurity(transportSecurity);
        transportSecurityChanged = true;
        return this;
    }

    synchronized void inUse() {
        useCount++;
        lastUsed = System.currentTimeMillis();
    }

    ChannelCredentials getChannelCredentials() {
        return TlsChannelCredentials.create();
    }

    synchronized ManagedChannel getChannel() {
        if (!transportSecurityChanged && channel != null) {
            return channel;
        }
        
        ManagedChannelBuilder<?> channelBuilder;

        if (address.isInProcess()) {
            channelBuilder = InProcessChannelBuilder.forName(address.getName());
        } else if (address.getTransportSecurity()) {
            channelBuilder = Grpc.newChannelBuilder(address.toString(), getChannelCredentials()).overrideAuthority("127.0.0.1");
        } else {
            channelBuilder = ManagedChannelBuilder.forTarget(address.toString()).usePlaintext();
        }

        channel = channelBuilder
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
