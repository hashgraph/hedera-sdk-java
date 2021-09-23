package com.hedera.hashgraph.sdk;

import io.grpc.ChannelCredentials;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import io.grpc.inprocess.InProcessChannelBuilder;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

abstract class ManagedNode {
    protected final ExecutorService executor;

    protected final ManagedNodeAddress address;
    protected long lastUsed = 0;
    protected long useCount = 0;

    @Nullable
    protected ManagedChannel channel = null;

    protected ManagedNode(ManagedNodeAddress address, ExecutorService executor) {
        this.executor = executor;
        this.address = address;
    }

    protected ManagedNode(ManagedNode node, ManagedNodeAddress address) {
        this.executor = node.executor;
        this.address = address;
        this.lastUsed = node.lastUsed;
        this.useCount = node.useCount;
    }

    public ManagedNodeAddress getAddress() {
        return address;
    }

    public synchronized void inUse() {
        useCount++;
        lastUsed = System.currentTimeMillis();
    }

    public ChannelCredentials getChannelCredentials() {
        return TlsChannelCredentials.create();
    }

    public synchronized ManagedChannel getChannel() {
        if (channel != null) {
            return channel;
        }

        ManagedChannelBuilder<?> channelBuilder;

        if (address.isInProcess()) {
            channelBuilder = InProcessChannelBuilder.forName(Objects.requireNonNull(address.getName()));
        } else if (address.isTransportSecurity()) {
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

    public synchronized void close(Duration timeout) throws InterruptedException {
        if (channel != null) {
            channel.shutdown();
            channel.awaitTermination(timeout.getSeconds(), TimeUnit.SECONDS);
            channel = null;
        }
    }

    private String getUserAgent() {
        var thePackage = getClass().getPackage();
        var implementationVersion = thePackage != null ? thePackage.getImplementationVersion() : null;
        return "hedera-sdk-java/" + ((implementationVersion != null) ? ("v" + implementationVersion) : "DEV");
    }
}
