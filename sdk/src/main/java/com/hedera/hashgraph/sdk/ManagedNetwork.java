package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import java8.util.Lists;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Abstracts away most of the similar functionality between {@link Network} and {@link MirrorNetwork}
 *
 * @param <ManagedNetworkT> - The network that is extending this class. This is used for builder pattern setter methods.
 * @param <KeyT> - The identifying type for the network.
 * @param <ManagedNodeT> - The specific node type for this network.
 */
abstract class ManagedNetwork<
    ManagedNetworkT extends ManagedNetwork<ManagedNetworkT, KeyT, ManagedNodeT>,
    KeyT,
    ManagedNodeT extends ManagedNode<ManagedNodeT, KeyT>> {
    protected static final Integer DEFAULT_MAX_NODE_ATTEMPTS = -1;

    protected final ExecutorService executor;

    /**
     * Map of node identifiers to nodes. Used to quickly fetch node for identifier.
     */
    protected Map<KeyT, List<ManagedNodeT>> network = new ConcurrentHashMap<>();

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
     * The current maximum backoff for the nodes in the network. This backoff is used when nodes return a bad
     * gRPC status.
     */
    protected Duration maxBackoff = Client.DEFAULT_MAX_BACKOFF;

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
    synchronized ManagedNetworkT setNetworkName(@Nullable NetworkName networkName) {
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
    synchronized ManagedNetworkT setMaxNodeAttempts(int maxNodeAttempts) {
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
    synchronized ManagedNetworkT setMinBackoff(Duration minBackoff) {
        this.minBackoff = minBackoff;

        for (var node : nodes) {
            node.setMinBackoff(minBackoff);
        }

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    Duration getMaxBackoff() {
        return maxBackoff;
    }

    /**
     * Set the maximum backoff a node should use when receiving a bad gRPC status.
     *
     * @param maxBackoff
     * @return
     */
    synchronized ManagedNetworkT setMaxBackoff(Duration maxBackoff) {
        this.maxBackoff = maxBackoff;

        for (var node : nodes) {
            node.setMaxBackoff(maxBackoff);
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
    synchronized ManagedNetworkT setTransportSecurity(boolean transportSecurity) throws InterruptedException {
        if (this.transportSecurity != transportSecurity) {
            network.clear();

            for (int i = 0; i < nodes.size(); i++) {
                @Var var node = nodes.get(i);
                node.close(closeTimeout);

                node = transportSecurity ? node.toSecure() : node.toInsecure();

                nodes.set(i, node);
                getNodesForKey(node.getKey()).add(node);
            }
        }

        this.transportSecurity = transportSecurity;

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }


    Duration getCloseTimeout() {
        return closeTimeout;
    }

    synchronized ManagedNetworkT setCloseTimeout(Duration closeTimeout) {
        this.closeTimeout = closeTimeout;

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    protected abstract ManagedNodeT createNodeFromNetworkEntry(Map.Entry<String, KeyT> entry);

    /**
     * Returns a list of index in descending order to remove from the current node list.
     *
     * Descending order is important here because {@link ManagedNetwork#setNetwork(Map<String, KeyT>)} uses a for-each loop.
     *
     * @param network - the new network
     * @return - list of indexes in descending order
     */
    protected List<Integer> getNodesToRemove(Map<String, KeyT> network) {
        var nodes = new ArrayList<Integer>(this.nodes.size());

        for (int i = this.nodes.size() - 1; i >= 0; i--) {
            var node = this.nodes.get(i);

            if (!nodeIsInGivenNetwork(node, network)) {
                nodes.add(i);
            }
        }

        return nodes;
    }

    private boolean nodeIsInGivenNetwork(ManagedNodeT node, Map<String,KeyT> network) {
        for (var entry : network.entrySet()) {
            if (
                node.getKey().equals(entry.getValue()) &&
                node.address.equals(ManagedNodeAddress.fromString(entry.getKey()))
            ) {
                return true;
            }
        }
        return false;
    }

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
    synchronized ManagedNetworkT setNetwork(Map<String, KeyT> network) throws TimeoutException, InterruptedException {
        // getNodesToRemove() should always return the list in reverse order
        for (var index : getNodesToRemove(network)) {
            var stopAt = Instant.now().getEpochSecond() + closeTimeout.getSeconds();
            var remainingTime = stopAt - Instant.now().getEpochSecond();
            var node = nodes.get(index);

            // Exit early if we have no time remaining
            if (remainingTime <= 0) {
                throw new TimeoutException("Failed to properly shutdown all channels");
            }

            removeNodeFromNetwork(node);
            node.close(Duration.ofSeconds(remainingTime));
            this.nodes.remove(index.intValue());
        }

        // Add new nodes that are not present in the list
        for (var entry : network.entrySet()) {
            var nodesForKey = getNodesForKey(entry.getValue());

            // Only add nodes which don't already exist in our network map
            if (!addressIsInNodeList(entry.getKey(), nodesForKey)) {
                var node = createNodeFromNetworkEntry(entry);
                nodesForKey.add(node);
                this.nodes.add(node);
            }
        }

        Collections.shuffle(nodes);
        for (var nodeList : this.network.values()) {
            Collections.shuffle(nodeList);
        }

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    private void removeNodeFromNetwork(ManagedNodeT node) {
        var nodesForKey = this.network.get(node.getKey());
        nodesForKey.remove(node);
        if (nodesForKey.isEmpty()) {
            this.network.remove(node.getKey());
        }
    }

    private List<ManagedNodeT> getNodesForKey(KeyT key) {
        if (network.containsKey(key)) {
            return network.get(key);
        } else {
            var newList = new ArrayList<ManagedNodeT>();
            network.put(key, newList);
            return newList;
        }
    }

    private boolean addressIsInNodeList(String addressString, List<ManagedNodeT> nodes) {
        var address = ManagedNodeAddress.fromString(addressString);
        for (var node : nodes) {
            if (node.address.equals(address)) {
                return true;
            }
        }
        return false;
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
                    removeNodeFromNetwork(node);
                    nodes.remove(i);
                }
            }
        }
    }

    /**
     * Returns `count` number of the most healthy nodes. Healthy-ness is determined by sort order; leftmost being most
     * healthy. This will also remove any nodes which have hit or exceeded {@link ManagedNetwork#maxNodeAttempts}.
     *
     * Returns a list of nodes where each node has a unique key.
     *
     * @param count - number of nodes to return
     * @return
     * @throws InterruptedException
     */
    protected synchronized List<ManagedNodeT> getNumberOfMostHealthyNodes(int count) throws InterruptedException {
        Collections.sort(nodes);
        removeDeadNodes();
        for (var nodeList : network.values()) {
            Collections.sort(nodeList);
        }

        var returnSize = Math.min(count, this.network.size());
        var returnNodes = new HashMap<KeyT, ManagedNodeT>(returnSize);

        for (var node : this.nodes) {
            if (returnNodes.size() >= returnSize) {
                break;
            }
            if (!returnNodes.containsKey(node.getKey())) {
                returnNodes.put(node.getKey(), node);
            }
        }

        return Lists.copyOf(returnNodes.values());
    }

    /**
     * Close the network with the {@link ManagedNetwork#closeTimeout} duration
     *
     * @throws TimeoutException
     * @throws InterruptedException
     */
    synchronized void close() throws TimeoutException, InterruptedException {
        close(closeTimeout);
    }

    /**
     * Close the network with a specific timeout duration
     *
     * @param timeout
     * @throws TimeoutException
     * @throws InterruptedException
     */
    synchronized void close(Duration timeout) throws TimeoutException, InterruptedException {
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
                throw new TimeoutException("Failed to properly shutdown all channels");
            }

            if (node.channel != null) {
                // InterruptedException needs to be caught here to prevent early exist without releasing lock
                try {
                    node.channel.awaitTermination(stopAt - Instant.now().getEpochSecond(), TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        nodes.clear();
        network.clear();
    }
}
