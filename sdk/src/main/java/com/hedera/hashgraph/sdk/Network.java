package com.hedera.hashgraph.sdk;

import com.google.common.collect.HashBiMap;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class Network {
    static final Integer DEFAULT_MAX_NODE_ATTEMPTS = -1;
    final ExecutorService executor;
    final Semaphore lock = new Semaphore(1);
    HashMap<String, AccountId> network = new HashMap<>();
    HashMap<AccountId, Node> networkNodes = new HashMap<>();
    @Nullable
    NetworkName networkName = null;

    List<Node> nodes = new ArrayList<>();
    @Nullable
    Integer maxNodesPerTransaction = null;
    private int maxNodeAttempts = DEFAULT_MAX_NODE_ATTEMPTS;
    private Duration nodeWaitTime = Duration.ofMillis(250);

    Network(ExecutorService executor, Map<String, AccountId> network) {
        this.executor = executor;

        try {
            setNetwork(network);
        } catch (InterruptedException e) {
            // This should never occur. The network is empty.
        } catch (TimeoutException e) {
            // This should never occur. The network is empty.
        }
    }

    void setNetwork(Map<String, AccountId> network) throws InterruptedException, TimeoutException {
        lock.acquire();

        // Bypass the more complex code if the network is empty
        if (this.network.isEmpty()) {
            this.network = new HashMap<>(network);

            for (var entry : network.entrySet()) {
                var node = new Node(entry.getValue(), entry.getKey(), nodeWaitTime.toMillis(), executor);
                this.networkNodes.put(entry.getValue(), node);
                this.nodes.add(node);
            }

            Collections.shuffle(nodes);

            lock.release();
            return;
        }

        this.network = new HashMap<>(network);
        var inverted = HashBiMap.create(network).inverse();
        var newNodeAccountIds = network.values();
        var stopAt = Instant.now().getEpochSecond() + Duration.ofSeconds(30).getSeconds();

        // Remove nodes that don't exist in new network or that have a different
        // address for the same AccountId
        for (int i = 0; i < nodes.size(); i++) {
            if (stopAt - Instant.now().getEpochSecond() == 0) {
                lock.release();
                throw new TimeoutException("Failed to properly shutdown all channels");
            }

            if (
                !newNodeAccountIds.contains(nodes.get(i).accountId) ||
                    !Objects.requireNonNull(inverted.get(nodes.get(i).accountId)).equals(nodes.get(i).address)
            ) {
                networkNodes.remove(nodes.get(i).accountId);
                nodes.get(i).close(stopAt - Instant.now().toEpochMilli());
                nodes.remove(i);
                i--;
            }
        }

        // Add new nodes that are not present in the list
        for (var entry : inverted.entrySet()) {
            if (networkNodes.get(entry.getKey()) == null) {
                var node = new Node(entry.getKey(), entry.getValue(), nodeWaitTime.toMillis(), executor);

                nodes.add(node);
                networkNodes.put(entry.getKey(), node);
            }
        }

        Collections.shuffle(nodes);

        lock.release();
    }


    /**
     * Pick 1/3 of the nodes sorted by health and expected delay from the network.
     * This is used by Query and Transaction for selecting node AccountId's.
     *
     * @return {@link java.util.List<com.hedera.hashgraph.sdk.AccountId>}
     */
    List<AccountId> getNodeAccountIdsForExecute() throws InterruptedException {
        lock.acquire();

        Collections.sort(nodes);

        // Remove nodes which have surpassed max attempts
        if (maxNodeAttempts > 0) {
            for (var i = 0; i < nodes.size(); i++) {
                var node = Objects.requireNonNull(nodes.get(i));
                if (node.attempts >= maxNodeAttempts) {
                    node.close(30);
                    nodes.remove(i);
                    network.remove(node.address);
                    networkNodes.remove(node.accountId);
                    i--;
                }
            }
        }

        List<AccountId> resultNodeAccountIds = new ArrayList<>();

        for (int i = 0; i < getNumberOfNodesForTransaction(); i++) {
            resultNodeAccountIds.add(nodes.get(i).accountId);
        }

        lock.release();

        return resultNodeAccountIds;
    }

    void setMaxNodesPerTransaction(int maxNodesPerTransaction) {
        this.maxNodesPerTransaction = maxNodesPerTransaction;
    }

    int getMaxNodeAttempts() {
        return maxNodeAttempts;
    }

    void setMaxNodeAttempts(int maxNodeAttempts) {
        this.maxNodeAttempts = maxNodeAttempts;
    }

    Duration getNodeWaitTime() {
        return nodeWaitTime;
    }

    void setNodeWaitTime(Duration nodeWaitTime) {
        this.nodeWaitTime = nodeWaitTime;
        for (var node : nodes) {
            node.setWaitTime(nodeWaitTime.toMillis());
        }
    }

    int getNumberOfNodesForTransaction() {
        if (maxNodesPerTransaction != null) {
            return Math.min(maxNodesPerTransaction, nodes.size());
        } else {
            return (nodes.size() + 3 - 1) / 3;
        }
    }

    void close(Duration timeout) throws TimeoutException {
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        var stopAt = Instant.now().getEpochSecond() + timeout.getSeconds();

        for (var node : nodes) {
            if (node.channel != null) {
                node.channel = node.channel.shutdown();
            }
        }

        for (var node : nodes) {
            if (stopAt - Instant.now().getEpochSecond() == 0) {
                lock.release();
                throw new TimeoutException("Failed to properly shutdown all channels");
            }

            if (node.channel != null) {
                try {
                    node.channel.awaitTermination(stopAt - Instant.now().getEpochSecond(), TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    lock.release();
                    throw new RuntimeException(e);
                }
            }
        }

        nodes.clear();
        networkNodes.clear();
        network.clear();

        lock.release();
    }
}
