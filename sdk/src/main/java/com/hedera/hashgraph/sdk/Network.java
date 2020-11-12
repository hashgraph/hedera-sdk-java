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

class Network {
    Map<String, AccountId> network = new HashMap<>();
    Map<AccountId, Node> networkNodes = new HashMap<>();
    List<Node> nodes = new ArrayList<>();
    final ExecutorService executor;
    long nodeLastUsedAt = Instant.now().getEpochSecond();

    Network(ExecutorService executor, Map<String, AccountId> network) {
        this.executor = executor;

        try {
            setNetwork(network);
        } catch (InterruptedException e) {
            // This should never occur. The network is empty.
        }
    }

    void setNetwork(Map<String, AccountId> network) throws InterruptedException {
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

        // Remove nodes that don't exist in new network or that have a different
        // address for the same AccountId
        for (int i = 0; i < nodes.size(); i++) {
            if (
                !newNodeAccountIds.contains(nodes.get(i).accountId) ||
                !inverted.get(nodes.get(i).accountId).equals(nodes.get(i).address)
            ) {
                networkNodes.remove(nodes.get(i).accountId);
                nodes.get(i).close();
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
        if (nodeLastUsedAt + 1 < Instant.now().getEpochSecond()) {
            nodes.sort((a,b) -> {
                if (a.isHealthy() && b.isHealthy()) {
                    return 0;
                } else if (a.isHealthy() && !b.isHealthy()) {
                    return -1;
                } else if (!a.isHealthy() && b.isHealthy()) {
                    return 1;
                } else {
                    var aLastUsed = a.lastUsed != null ? a.lastUsed : 0;
                    var bLastUsed = b.lastUsed != null ? b.lastUsed : 0;

                    if (aLastUsed + a.delay < bLastUsed + b.delay) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            });

            this.nodeLastUsedAt = Instant.now().toEpochMilli();
        }

        List<AccountId> resultNodeAccountIds = new ArrayList<>();

        for (int i = 0; i < getNumberOfNodesForTransaction(); i++) {
            resultNodeAccountIds.add(nodes.get(i).accountId);
        }

        return resultNodeAccountIds;
    }

    int getNumberOfNodesForTransaction() {
        return (network.size() + 3 - 1) / 3;
    }

    void close(Duration timeout) {
        for (var node : nodes) {
            if (node.channel != null) {
                node.channel.shutdown();
            }
        }

        for (var node: nodes) {
            if (node.channel != null) {
                try {
                    node.channel.awaitTermination(timeout.getSeconds(), TimeUnit.SECONDS);
                } catch (InterruptedException e ) {
                    throw new RuntimeException(e);
                }
            }
        }

        nodes.clear();
        networkNodes.clear();
        network.clear();
    }
}
