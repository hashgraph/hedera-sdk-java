package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import io.grpc.ChannelCredentials;
import io.grpc.ConnectivityState;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.TlsChannelCredentials;
import io.grpc.inprocess.InProcessChannelBuilder;
import java8.util.Objects;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java8.util.concurrent.CompletableFuture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

abstract class ManagedNode<N extends ManagedNode<N, KeyT>, KeyT> implements Comparable<ManagedNode<N, KeyT>> {
    private static final int GET_STATE_INTERVAL_MILLIS = 50;
    private static final int GET_STATE_TIMEOUT_MILLIS = 10000;
    private static final int GET_STATE_MAX_ATTEMPTS = GET_STATE_TIMEOUT_MILLIS / GET_STATE_INTERVAL_MILLIS;
    private boolean hasConnected = false;

    protected final ExecutorService executor;

    /**
     * Address of this node
     */
    protected final ManagedNodeAddress address;

    /**
     * Timestamp of when the last time this node was used in milliseconds.
     * This field is used for healthy-ness calculation.
     */
    protected long lastUsed = 0;

    /**
     * Amount of times this node has been used. "Used" means the channel was used to submit a request.
     * This field is used for healthy-ness calculation
     */
    protected long useCount = 0;

    /**
     * Timestamp of when this node will be considered healthy again
     */
    protected long backoffUntil;

    /**
     * The current backoff duration. Uses exponential backoff so think 1s, 2s, 4s, 8s, etc until maxBackoff is hit
     */
    protected Duration currentBackoff;

    /**
     * Minimum backoff used by node when receiving a bad gRPC status
     */
    protected Duration minBackoff;

    /**
     * Maximum backoff used by node when receiving a bad gRPC status
     */
    protected Duration maxBackoff;

    /**
     * Number of times this node has received a bad gRPC status
     */
    protected long badGrpcStatusCount;

    @Nullable
    protected ManagedChannel channel = null;

    protected ManagedNode(ManagedNodeAddress address, ExecutorService executor) {
        this.executor = executor;
        this.address = address;
        this.currentBackoff = Client.DEFAULT_MIN_NODE_BACKOFF;
        this.minBackoff = Client.DEFAULT_MIN_NODE_BACKOFF;
        this.maxBackoff = Client.DEFAULT_MAX_NODE_BACKOFF;
    }

    protected ManagedNode(N node, ManagedNodeAddress address) {
        this.address = address;

        this.executor = node.executor;
        this.minBackoff = node.minBackoff;
        this.maxBackoff = node.maxBackoff;
        this.backoffUntil = node.backoffUntil;
        this.currentBackoff = node.currentBackoff;
        this.badGrpcStatusCount = node.badGrpcStatusCount;
        this.lastUsed = node.lastUsed;
        this.useCount = node.useCount;
    }

    protected String getAuthority() {
        return "127.0.0.1";
    }

    /**
     * Create an insecure version of this node
     *
     * @return
     */
    abstract N toInsecure();

    /**
     * Create a secure version of this node
     * @return
     */
    abstract N toSecure();

    abstract KeyT getKey();

    /**
     * Get the address of this node
     *
     * @return
     */
    ManagedNodeAddress getAddress() {
        return address;
    }

    /**
     * Get the minimum backoff
     * @return
     */
    Duration getMinBackoff() {
        return minBackoff;
    }

    /**
     * Set the minimum backoff
     *
     * @param minBackoff
     * @return
     */
    N setMinBackoff(Duration minBackoff) {
        if (currentBackoff == this.minBackoff) {
            currentBackoff = minBackoff;
        }
        this.minBackoff = minBackoff;

        // noinspection unchecked
        return (N) this;
    }

    /**
     * Get the maximum backoff
     * @return
     */
    Duration getMaxBackoff() {
        return maxBackoff;
    }

    /**
     * Set the minimum backoff
     *
     * @param maxBackoff
     * @return
     */
    N setMaxBackoff(Duration maxBackoff) {
        this.maxBackoff = ManagedNode.this.maxBackoff;

        // noinspection unchecked
        return (N) this;
    }

    /**
     * Get the number of times this node has received a bad gRPC status
     * @return
     */
    long getBadGrpcStatusCount() {
        return badGrpcStatusCount;
    }

    long unhealthyBackoffRemaining() {
        return Math.max(0, backoffUntil - System.currentTimeMillis());
    }

    /**
     * Determines if this is node is healthy.
     * Healthy means the node has either not received any bad gRPC statuses, or if it has received bad gRPC status then
     * the node backed off for a period of time.
     *
     * @return
     */
    boolean isHealthy() {
        return backoffUntil < System.currentTimeMillis();
    }

    /**
     * Used when a node has received a bad gRPC status
     */
    synchronized void increaseDelay() {
        this.badGrpcStatusCount++;
        this.backoffUntil = System.currentTimeMillis() + this.currentBackoff.toMillis();
        this.currentBackoff = Duration.ofMillis(Math.min(this.currentBackoff.toMillis() * 2, this.maxBackoff.toMillis()));
    }

    /**
     * Used when a node has not received a bad gRPC status.
     * This means on each request that doesn't get a bad gRPC status the current backoff will be lowered. The point of
     * this is to allow a node which has been performing poorly (receiving several bad gRPC status) to become used again
     * once it stops receiving bad gRPC statuses.
     */
    synchronized void decreaseDelay() {
        this.currentBackoff = Duration.ofMillis(Math.max(this.currentBackoff.toMillis() / 2, minBackoff.toMillis()));
    }

    /**
     * Get the amount of time the node has to wait until it's healthy again
     *
     * @return
     */
    long getRemainingTimeForBackoff() {
        return backoffUntil - System.currentTimeMillis();
    }

    /**
     * Create TLS credentials when transport security is enabled
     *
     * @return
     */
    ChannelCredentials getChannelCredentials() {
        return TlsChannelCredentials.create();
    }

    synchronized void inUse() {
        useCount++;
        lastUsed = System.currentTimeMillis();
    }

    /**
     * Get the gRPC channel for this node
     *
     * @return
     */
    synchronized ManagedChannel getChannel() {
        inUse();

        if (channel != null) {
            return channel;
        }

        ManagedChannelBuilder<?> channelBuilder;

        if (address.isInProcess()) {
            channelBuilder = InProcessChannelBuilder.forName(Objects.requireNonNull(address.getName()));
        } else if (address.isTransportSecurity()) {
            channelBuilder = Grpc.newChannelBuilder(address.toString(), getChannelCredentials());

            String authority = getAuthority();
            if (authority != null) {
                channelBuilder = channelBuilder.overrideAuthority(authority);
            }
        } else {
            channelBuilder = ManagedChannelBuilder.forTarget(address.toString()).usePlaintext();
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
        hasConnected = (getChannel().getState(true) == ConnectivityState.READY);
        try {
            for (@Var int i = 0; i < GET_STATE_MAX_ATTEMPTS && !hasConnected; i++) {
                TimeUnit.MILLISECONDS.sleep(GET_STATE_INTERVAL_MILLIS);
                hasConnected = (getChannel().getState(true) == ConnectivityState.READY);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Close the current nodes channel
     *
     * @param timeout
     * @throws InterruptedException
     */
    synchronized void close(Duration timeout) throws InterruptedException {
        if (channel != null) {
            channel.shutdown();
            channel.awaitTermination(timeout.getSeconds(), TimeUnit.SECONDS);
            channel = null;
        }
    }

    /**
     * Compares one node to another.  If node a < node b, use of node a will be prioritized over node b.
     *
     * @param node
     * @return
     */
    @Override
    public int compareTo(ManagedNode<N, KeyT> node) {
        int backoffRemainingComparison = Long.compare(this.unhealthyBackoffRemaining(), node.unhealthyBackoffRemaining());
        if (backoffRemainingComparison != 0) {
            return backoffRemainingComparison;
        }

        int currentBackoffComparison = this.currentBackoff.compareTo(node.currentBackoff);
        if (currentBackoffComparison != 0) {
            return currentBackoffComparison;
        }

        int badGrpcComparison = Long.compare(this.badGrpcStatusCount, node.badGrpcStatusCount);
        if (badGrpcComparison != 0) {
            return badGrpcComparison;
        }

        int useCountComparison = Long.compare(this.useCount, node.useCount);
        if (useCountComparison != 0) {
            return useCountComparison;
        }

        return Long.compare(this.lastUsed, node.lastUsed);
    }

    private String getUserAgent() {
        var thePackage = getClass().getPackage();
        var implementationVersion = thePackage != null ? thePackage.getImplementationVersion() : null;
        return "hedera-sdk-java/" + ((implementationVersion != null) ? ("v" + implementationVersion) : "DEV");
    }
}
