package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Abstracts await most of the similar functionality between {@link Network} and {@link MirrorNetwork}
 *
 * @param <ManagedNetworkT> - The network that is extending this class. This is used for builder pattern setter methods.
 * @param <KeyT> - The identifying type for the network.
 * @param <ManagedNodeT> - The specific node type for this network.
 * @param <SdkNetworkT> - The type used by users to create this network.
 * @param <SdkNetworkEntryT> - The type used to iterate over the network.
 */
abstract class ManagedNetwork<
    ManagedNetworkT extends ManagedNetwork<ManagedNetworkT, KeyT, ManagedNodeT, SdkNetworkT, SdkNetworkEntryT>,
    KeyT,
    ManagedNodeT extends ManagedNode<ManagedNodeT, KeyT>,
    SdkNetworkT,
    SdkNetworkEntryT> {
    protected static final Integer DEFAULT_MAX_NODE_ATTEMPTS = -1;
    protected final Semaphore lock = new Semaphore(1);

    protected final ExecutorService executor;

    /**
     * Map of node identifiers to nodes. Used to quickly fetch node for identifier.
     */
    protected HashMap<KeyT, ManagedNodeT> network = new HashMap<>();

    /**
     * The list of nodes. This list is continuously sorted so the leftmost nodes are the "healthiest" {@link ManagedNode#isHealthy()}
     */
    protected List<ManagedNodeT> nodes = new ArrayList<>();

    /**
     * The current minimum backoff for the nodes in the network. This backoff is used when nodes return a bad
     * gRPC status.
     */
    protected Duration minBackoff = Client.DEFAULT_MIN_BACKOFF;

    /**
     * Timeout for closing either a single node when setting a new network, or closing the entire network.
     */
    protected Duration closeTimeout = Client.DEFAULT_CLOSE_TIMEOUT;

    /**
     * Limit for how many times we retry a node which has returned a bad gRPC status
     */
    protected int maxNodeAttempts = DEFAULT_MAX_NODE_ATTEMPTS;

    /**
     * Is the network using transport security
     */
    protected boolean transportSecurity;

    /**
     * The name of the network. This corresponds to ledger ID in entity ID checksum calculations
     */
    @Nullable
    private NetworkName networkName;

    protected ManagedNetwork(ExecutorService executor) {
        this.executor = executor;
    }

    @Nullable
    NetworkName getNetworkName() {
        return networkName;
    }

    /**
     * Set the new network name for this network. Network names are used for TLS certificate checking and entity ID
     * checksum validation.
     *
     * @param networkName
     * @return
     */
    ManagedNetworkT setNetworkName(@Nullable NetworkName networkName) {
        this.networkName = networkName;

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    int getMaxNodeAttempts() {
        return maxNodeAttempts;
    }

    /**
     * Set the max number of times a node can return a bad gRPC status before we remove it from the list.
     *
     * @param maxNodeAttempts
     * @return
     */
    ManagedNetworkT setMaxNodeAttempts(int maxNodeAttempts) {
        this.maxNodeAttempts = maxNodeAttempts;

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    Duration getMinBackoff() {
        return minBackoff;
    }

    /**
     * Set the minimum backoff a node should use when receiving a bad gRPC status.
     *
     * @param minBackoff
     * @return
     */
    ManagedNetworkT setMinBackoff(Duration minBackoff) {
        this.minBackoff = minBackoff;

        for (var node : nodes) {
            node.setMinBackoff(minBackoff);
        }

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    boolean isTransportSecurity() {
        return transportSecurity;
    }

    /**
     * Enable or disable transport security (TLS).
     *
     * @param transportSecurity
     * @return
     * @throws InterruptedException
     */
    ManagedNetworkT setTransportSecurity(boolean transportSecurity) throws InterruptedException {
        if (this.transportSecurity != transportSecurity) {
            lock.acquire();

            network.clear();

            for (int i = 0; i < nodes.size(); i++) {
                @Var var node = nodes.get(i);
                node.close(closeTimeout);

                node = transportSecurity ? node.toSecure() : node.toInsecure();

                nodes.set(i, node);
                network.put(node.getKey(), node);
            }

            lock.release();
        }

        this.transportSecurity = transportSecurity;

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }


    Duration getCloseTimeout() {
        return closeTimeout;
    }

    ManagedNetworkT setCloseTimeout(Duration closeTimeout) {
        this.closeTimeout = closeTimeout;

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    protected abstract Iterable<SdkNetworkEntryT> createIterableNetwork(SdkNetworkT network);

    protected abstract ManagedNodeT createNodeFromNetworkEntry(SdkNetworkEntryT entry);

    /**
     * Returns a list of index in descending order to remove from the current node list.
     *
     * Descending order is important here because {@link ManagedNetwork#setNetwork(Object)} uses a for-each loop.
     *
     * @param network - the new network
     * @return - list of indexes in descending order
     */
    protected abstract List<Integer> getNodesToRemove(SdkNetworkT network);

    protected abstract boolean checkNetworkContainsEntry(SdkNetworkEntryT entry);

    /**
     * Intelligently overwrites the current network.
     *
     * Shutdown and remove any node from the current network if the new network doesn't contain it. This includes
     * checking both the URL and {@link AccountId} when the network is a {@link Network}.
     *
     * Add any nodes from the new network that don't already exist in the network.
     *
     * @param network - The new network
     * @return - {@code this}
     * @throws TimeoutException - when shutting down nodes
     * @throws InterruptedException - when acquiring the lock
     */
    ManagedNetworkT setNetwork(SdkNetworkT network) throws TimeoutException, InterruptedException {
        lock.acquire();

        var iterableNetwork = createIterableNetwork(network);

        // Sort circuit the rest of the setNetwork logic if nodes is empty
        if (nodes.isEmpty()) {
            for (var entry : iterableNetwork) {
                var node = createNodeFromNetworkEntry(entry);
                this.network.put(node.getKey(), node);
                this.nodes.add(node);
            }

            Collections.shuffle(nodes);
            lock.release();

            // noinspection unchecked
            return (ManagedNetworkT) this;
        }

        // getNodesToRemove() should always return the list in reverse order
        for (var index : getNodesToRemove(network)) {
            var stopAt = Instant.now().getEpochSecond() + closeTimeout.getSeconds();
            var remainingTime = stopAt - Instant.now().getEpochSecond();
            var node = nodes.get(index);

            // Exit early if we have no time remaining
            if (remainingTime <= 0) {
                lock.release();
                throw new TimeoutException("Failed to properly shutdown all channels");
            }

            this.network.remove(node.getKey());
            node.close(Duration.ofSeconds(remainingTime));
            this.nodes.remove(index.intValue());
        }

        // Add new nodes that are not present in the list
        for (var entry : iterableNetwork) {
            // Only add nodes which don't already exist in our network map
            if (!checkNetworkContainsEntry(entry)) {
                var node = createNodeFromNetworkEntry(entry);
                this.network.put(node.getKey(), node);
                this.nodes.add(node);
            }
        }

        Collections.shuffle(nodes);
        lock.release();

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    /**
     * Remove any nodes from the network when they've exceeded the {@link ManagedNetwork#maxNodeAttempts} limit
     *
     * @throws InterruptedException - when shutting down nodes
     */
    protected void removeDeadNodes() throws InterruptedException {
        if (maxNodeAttempts > 0) {
            for (int i = nodes.size() - 1; i >= 0; i--) {
                var node = Objects.requireNonNull(nodes.get(i));

                if (node.getAttempts() >= maxNodeAttempts) {
                    node.close(closeTimeout);
                    network.remove(node.getKey());
                    nodes.remove(i);
                }
            }
        }
    }

    /**
     * Returns `count` number of the most healthy nodes. Healthy-ness is determined by sort order; leftmost being most
     * healthy. This will also remove any nodes which have hit or exceeded {@link ManagedNetwork#maxNodeAttempts}.
     *
     * @param count - number of nodes to return
     * @return
     * @throws InterruptedException
     */
    protected List<ManagedNodeT> getNumberOfMostHealthyNodes(int count) throws InterruptedException {
        lock.acquire();

        Collections.sort(nodes);
        removeDeadNodes();

        List<ManagedNodeT> nodes = new ArrayList<>(count);

        var size = Math.min(count, this.nodes.size());
        for (int i = 0; i < size; i++) {
            nodes.add(this.nodes.get(i));
        }

        lock.release();

        return nodes;
    }

    /**
     * Close the network with the {@link ManagedNetwork#closeTimeout} duration
     *
     * @throws TimeoutException
     * @throws InterruptedException
     */
    void close() throws TimeoutException, InterruptedException {
        close(closeTimeout);
    }

    /**
     * Close the network with a specific timeout duration
     *
     * @param timeout
     * @throws TimeoutException
     * @throws InterruptedException
     */
    void close(Duration timeout) throws TimeoutException, InterruptedException {
        lock.acquire();

        var stopAt = Instant.now().getEpochSecond() + timeout.getSeconds();

        // Start the shutdown process on all nodes
        for (var node : nodes) {
            if (node.channel != null) {
                node.channel = node.channel.shutdown();
            }
        }

        // Await termination for all nodes
        for (var node : nodes) {
            if (stopAt - Instant.now().getEpochSecond() == 0) {
                lock.release();
                throw new TimeoutException("Failed to properly shutdown all channels");
            }

            if (node.channel != null) {
                // InterruptedException needs to be caught here to prevent early exist without releasing lock
                try {
                    node.channel.awaitTermination(stopAt - Instant.now().getEpochSecond(), TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    lock.release();
                    throw new RuntimeException(e);
                }
            }
        }

        nodes.clear();
        network.clear();
        lock.release();
    }
}
