/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import java8.util.Lists;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
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
    protected static final Random random = new Random();

    protected final ExecutorService executor;

    /**
     * Map of node identifiers to nodes. Used to quickly fetch node for identifier.
     */
    protected Map<KeyT, List<ManagedNodeT>> network = new ConcurrentHashMap<>();

    /**
     * The list of all nodes.
     */
    protected List<ManagedNodeT> nodes = new ArrayList<>();

    /**
     * The list of currently healthy nodes.
     */
    protected List<ManagedNodeT> healthyNodes = new ArrayList<>();

    /**
     * The current minimum backoff for the nodes in the network. This backoff is used when nodes return a bad
     * gRPC status.
     */
    protected Duration minNodeBackoff = Client.DEFAULT_MIN_NODE_BACKOFF;

    /**
     * The current maximum backoff for the nodes in the network. This backoff is used when nodes return a bad
     * gRPC status.
     */
    protected Duration maxNodeBackoff = Client.DEFAULT_MAX_NODE_BACKOFF;

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
     * The min time to wait before attempting to readmit nodes.
     */
    protected Duration minNodeReadmitTime = Client.DEFAULT_MIN_NODE_BACKOFF;

    /**
     * The max time to wait for readmitting nodes.
     */
    protected Duration maxNodeReadmitTime = Client.DEFAULT_MAX_NODE_BACKOFF;

    /**
     * The instant that readmission will happen after.
     */
    protected Instant earliestReadmitTime;

    /**
     * The name of the network. This corresponds to ledger ID in entity ID checksum calculations
     */
    @Nullable
    private LedgerId ledgerId;

    protected ManagedNetwork(ExecutorService executor) {
        this.executor = executor;
        earliestReadmitTime = Instant.now().plus(minNodeReadmitTime);
    }

    @Nullable
    LedgerId getLedgerId() {
        return ledgerId;
    }

    /**
     * Set the new LedgerId for this network. LedgerIds are used for TLS certificate checking and entity ID
     * checksum validation.
     *
     * @param ledgerId
     * @return
     */
    synchronized ManagedNetworkT setLedgerId(@Nullable LedgerId ledgerId) {
        this.ledgerId = ledgerId;

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

    Duration getMinNodeBackoff() {
        return minNodeBackoff;
    }

    /**
     * Set the minimum backoff a node should use when receiving a bad gRPC status.
     *
     * @param minNodeBackoff
     * @return
     */
    synchronized ManagedNetworkT setMinNodeBackoff(Duration minNodeBackoff) {
        this.minNodeBackoff = minNodeBackoff;

        for (var node : nodes) {
            node.setMinBackoff(minNodeBackoff);
        }

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    Duration getMaxNodeBackoff() {
        return maxNodeBackoff;
    }

    /**
     * Set the maximum backoff a node should use when receiving a bad gRPC status.
     *
     * @param maxNodeBackoff
     * @return
     */
    synchronized ManagedNetworkT setMaxNodeBackoff(Duration maxNodeBackoff) {
        this.maxNodeBackoff = maxNodeBackoff;

        for (var node : nodes) {
            node.setMaxBackoff(maxNodeBackoff);
        }

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    public Duration getMinNodeReadmitTime() {
        return minNodeReadmitTime;
    }

    public synchronized void setMinNodeReadmitTime(Duration minNodeReadmitTime) {
        this.minNodeReadmitTime = minNodeReadmitTime;

        for (var node : nodes) {
            node.readmitTime = Instant.now();
        }
    }

    public Duration getMaxNodeReadmitTime() {
        return maxNodeReadmitTime;
    }

    public synchronized void setMaxNodeReadmitTime(Duration maxNodeReadmitTime) {
        this.maxNodeReadmitTime = maxNodeReadmitTime;
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
        var newNodes = new ArrayList<ManagedNodeT>();
        var newHealthyNodes = new ArrayList<ManagedNodeT>(newNodes.size());
        var newNetwork = new HashMap<KeyT, List<ManagedNodeT>>(newNodes.size());
        var newNodeKeys = new HashSet<KeyT>();
        var newNodeAddresses = new HashSet<String>();

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

        for (var node : this.nodes) {
            newNodes.add(node);
            newNodeKeys.add(node.getKey());
            newNodeAddresses.add(node.address.toString());
        }

        for (var entry : network.entrySet()) {
            var node = createNodeFromNetworkEntry(entry);

            if (newNodeKeys.contains(node.getKey()) && newNodeAddresses.contains(node.getAddress().toString())) {
                continue;
            }

            newNodes.add(node);
        }

        for (var node : newNodes) {
            if (newNetwork.containsKey(node.getKey())) {
                newNetwork.get(node.getKey()).add(node);
            } else {
                var list = new ArrayList<ManagedNodeT>();
                list.add(node);
                newNetwork.put(node.getKey(), list);
            }

            newHealthyNodes.add(node);
        }

        // Atomically set all the variables
        nodes = newNodes;
        this.network = newNetwork;
        healthyNodes = newHealthyNodes;

        // noinspection unchecked
        return (ManagedNetworkT) this;
    }

    synchronized void increaseBackoff(ManagedNodeT node) {
        node.increaseBackoff();
        healthyNodes.remove(node);
    }

    void decreaseBackoff(ManagedNodeT node) {
        node.decreaseBackoff();
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

                if (node.getBadGrpcStatusCount() >= maxNodeAttempts) {
                    node.close(closeTimeout);
                    removeNodeFromNetwork(node);
                    nodes.remove(i);
                }
            }
        }
    }

    /**
     * Readmits nodes from the `nodes` list into the `healthyNodes` list when the time is passed the
     * {@code earliestReadmitTime}. While readmitting nodes the `earliestReadmitTime` will be updated to
     * a new value. This value is either the value of the node with the smallest readmission time from now,
     * or `minNodeReadmitTime` or `maxNodeReadmitTime`.
     */
    void readmitNodes() {
        var now = Instant.now();

        if (now.toEpochMilli() > earliestReadmitTime.toEpochMilli()) {
            var nextEarliestReadmitTime = now.plus(maxNodeReadmitTime);

            for (var node : this.nodes) {
                if (node.readmitTime.isAfter(now) && node.readmitTime.isBefore(nextEarliestReadmitTime)) {
                    nextEarliestReadmitTime = node.readmitTime;
                }
            }


            this.earliestReadmitTime = nextEarliestReadmitTime;
            if (this.earliestReadmitTime.isBefore(now.plus(minNodeReadmitTime))) {
                this.earliestReadmitTime = now.plus(minNodeReadmitTime);
            }

            outer: for (var i = 0; i < this.nodes.size(); i++) {
                // Check if `healthyNodes` already contains this node
                for (var j = 0; j < this.healthyNodes.size(); j++) {
                    if (this.nodes.get(i) == this.healthyNodes.get(j)) {
                        continue outer;
                    }
                }

                // If `healthyNodes` doesn't contain the node, check the `readmitTime` on the node
                if (this.nodes.get(i).readmitTime.isBefore(now)) {
                    this.healthyNodes.add(this.nodes.get(i));
                }
            }

        }
    }

    /**
     * Get a random node by key, or if null get a random healthy node.
     * @param key
     * @return
     */
    synchronized ManagedNodeT getNode(@Nullable KeyT key) {
        // Attempt to readmit nodes each time a node is fetched.
        // Note: Readmitting nodes will only happen periodically so calling it each time should not harm
        // performance.
        readmitNodes();

        if (key == null) {
            if (healthyNodes.isEmpty()) {
                throw new IllegalStateException("No healthy node was found");
            }

            return healthyNodes.get(random.nextInt(healthyNodes.size()));
        }

        var list = network.get(key);
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Returns `count` number of the most healthy nodes. Healthy-ness is determined by sort order; leftmost being most
     * healthy. This will also remove any nodes which have hit or exceeded {@link ManagedNetwork#maxNodeAttempts}.
     *
     * Returns a list of nodes where each node has a unique key.
     *
     * @param count - number of nodes to return
     * @return - List of nodes to use
     * @throws InterruptedException
     */
    protected synchronized List<ManagedNodeT> getNumberOfMostHealthyNodes(int count) throws InterruptedException {
        readmitNodes();
        removeDeadNodes();

        var returnNodes = new HashMap<KeyT, ManagedNodeT>(count);

        var revisedCount = Math.min(count, nodes.size());

        for (var i = 0; i < revisedCount; /* do not increment i here */ ) {
            var node = getNode(null);

            if (!returnNodes.containsKey(node.getKey())) {
                returnNodes.put(node.getKey(), node);
                i++;
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
