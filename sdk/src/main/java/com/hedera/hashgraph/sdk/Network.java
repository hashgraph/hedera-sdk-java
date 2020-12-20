package com.hedera.hashgraph.sdk;

import com.google.common.collect.HashBiMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class Network {
    Map<String, AccountId> network = new HashMap<>();
    Map<AccountId, Node> networkNodes = new HashMap<>();
    List<Node> nodes = new ArrayList<>();
    final ExecutorService executor;

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
        // Bypass the more complex code if the network is empty
        if (this.network.isEmpty()) {
            this.network = new HashMap<>(network);

            for (var entry : network.entrySet()) {
                var node = new Node(entry.getValue(), entry.getKey(), executor);
                this.networkNodes.put(entry.getValue(), node);
                this.nodes.add(node);
            }

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
                throw new TimeoutException("Failed to properly shutdown all channels");
            }

            if (
                !newNodeAccountIds.contains(nodes.get(i).accountId) ||
                    !inverted.get(nodes.get(i).accountId).equals(nodes.get(i).address)
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
                var node = new Node(entry.getKey(), entry.getValue(), executor);

                nodes.add(node);
                networkNodes.put(entry.getKey(), node);
            }
        }
    }


    /**
     * Pick 1/3 of the nodes sorted by health and expected delay from the network.
     * This is used by Query and Transaction for selecting node AccountId's.
     *
     * @return {@link java.util.List<com.hedera.hashgraph.sdk.AccountId>}
     */
    List<AccountId> getNodeAccountIdsForExecute() {
        nodes.sort(Node::compareTo);

        List<AccountId> resultNodeAccountIds = new ArrayList<>();

        for (int i = 0; i < getNumberOfNodesForTransaction(); i++) {
            resultNodeAccountIds.add(nodes.get(i).accountId);
        }

        return resultNodeAccountIds;
    }

    int getNumberOfNodesForTransaction() {
        int count = 0;
        for (var node : nodes) {
            count += node.isHealthy() ? 1 : 1;
        }
        return (count + 3 - 1) / 3;
    }

    void close(Duration timeout) throws TimeoutException {
        var stopAt = Instant.now().getEpochSecond() + timeout.getSeconds();

        for (var node : nodes) {
            if (node.channel != null) {
                node.channel = node.channel.shutdown();
            }
        }

        for (var node : nodes) {
            if (stopAt - Instant.now().getEpochSecond() == 0) {
                throw new TimeoutException("Failed to properly shutdown all channels");
            }

            if (node.channel != null) {
                try {
                    node.channel.awaitTermination(stopAt - Instant.now().getEpochSecond(), TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        nodes.clear();
        networkNodes.clear();
        network.clear();
    }
}
