package com.hedera.hashgraph.sdk;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.errorprone.annotations.Var;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Managed client for use on the Hedera Hashgraph network. */
public final class Client implements AutoCloseable {
    final ExecutorService executor;
    private final Iterator<AccountId> nodes;
    private final Map<AccountId, String> network;
    private Map<AccountId, ManagedChannel> channels;

    protected Client(Map<AccountId, String> network) {
        this.executor =
                Executors.newFixedThreadPool(
                        Runtime.getRuntime().availableProcessors(),
                        new ThreadFactoryBuilder().setNameFormat("hedera-sdk-%d").build());

        this.network = network;
        this.channels = new HashMap<>(network.size());

        // Take all given node account IDs, shuffle, and prepare an infinite iterator for use in
        // [getNextNodeId]
        var allNodes = new ArrayList<>(network.keySet());
        Collections.shuffle(allNodes, ThreadLocalSecureRandom.current());
        nodes = Iterables.cycle(allNodes).iterator();
    }

    /**
     * Construct a client given a set of nodes.
     *
     * <p>It is the responsibility of the caller to ensure that all nodes in the map are part of the
     * same Hedera network. Failure to do so will result in undefined behavior.
     *
     * <p>The client will load balance all requests to Hedera using a simple round-robin scheme to
     * chose nodes to send transactions to. For one transaction, at most 1/3 of the nodes will be
     * tried.
     *
     * @param network the map of node IDs to node addresses that make up the network.
     */
    public static Client forNetwork(Map<AccountId, String> network) {
        return new Client(network);
    }

    /**
     * Construct a Hedera client pre-configured for <a
     * href="https://docs.hedera.com/guides/mainnet/address-book#mainnet-address-book">Mainnet
     * access</a>.
     */
    public static Client forMainnet() {
        var network = new HashMap<AccountId, String>();
        network.put(new AccountId(3), "35.237.200.180:50211");
        network.put(new AccountId(4), "35.186.191.247:50211");
        network.put(new AccountId(5), "35.192.2.25:50211");
        network.put(new AccountId(6), "35.199.161.108:50211");
        network.put(new AccountId(7), "35.203.82.240:50211");
        network.put(new AccountId(8), "35.236.5.219:50211");
        network.put(new AccountId(9), "35.197.192.225:50211");
        network.put(new AccountId(10), "35.242.233.154:50211");
        network.put(new AccountId(11), "35.240.118.96:50211");
        network.put(new AccountId(12), "35.204.86.32:50211");

        return Client.forNetwork(network);
    }

    /**
     * Construct a Hedera client pre-configured for <a
     * href="https://docs.hedera.com/guides/testnet/nodes">Testnet access</a>.
     */
    public static Client forTestnet() {
        var network = new HashMap<AccountId, String>();
        network.put(new AccountId(3), "0.testnet.hedera.com:50211");
        network.put(new AccountId(4), "1.testnet.hedera.com:50211");
        network.put(new AccountId(5), "2.testnet.hedera.com:50211");
        network.put(new AccountId(6), "3.testnet.hedera.com:50211");

        return Client.forNetwork(network);
    }

    /**
     * Initiates an orderly shutdown of all channels (to the Hedera network) in which preexisting
     * transactions or queries continue but more would be immediately cancelled.
     *
     * <p>After this method returns, this client can be re-used. Channels will be re-established as
     * needed.
     */
    @Override
    public synchronized void close() {
        var channels = this.channels;
        this.channels = new HashMap<>(network.size());

        // initialize shutdown for all channels
        // this should not block
        for (var channel : channels.values()) {
            channel.shutdown();
        }

        // wait for all channels to shutdown
        for (var channel : channels.values()) {
            try {
                channel.awaitTermination(0, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // preemptively clear memory for the channels map
        channels.clear();
    }

    // Get the next node ID, following a round-robin distribution with a randomized start point
    synchronized AccountId getNextNodeId() {
        return nodes.next();
    }

    int getNumberOfNodesForSuperMajority() {
        return (network.size() + 3 - 1) / 3;
    }

    // Return or establish a channel for a given node ID
    synchronized ManagedChannel getChannel(AccountId nodeId) {
        @Var var channel = channels.get(nodeId);

        if (channel != null) {
            return channel;
        }

        var address = network.get(nodeId);

        channel =
                ManagedChannelBuilder.forTarget(address)
                        .usePlaintext()
                        // TODO: Get the actual project version
                        .userAgent("hedera-sdk-java/2.0.0-SNAPSHOT")
                        .executor(executor)
                        .build();

        channels.put(nodeId, channel);

        return channel;
    }
}
