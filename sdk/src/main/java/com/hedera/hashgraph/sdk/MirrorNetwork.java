package com.hedera.hashgraph.sdk;

import java8.util.Lists;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class MirrorNetwork {
    private final ExecutorService executor;
    private final Semaphore lock = new Semaphore(1);
    private HashSet<String> network = new HashSet<>();
    private List<MirrorNode> nodes = new ArrayList<>();
    private int index;

    private Duration closeTimeout = Client.DEFAULT_CLOSE_TIMEOUT;

    private boolean transportSecurity;
    private boolean transportSecurityChanged;

    private MirrorNetwork(ExecutorService executor, List<String> addresses) {
        this.executor = executor;

        try {
            setNetwork(addresses);
        } catch (InterruptedException | TimeoutException e) {
            // This should never occur. The network is empty.
        }
    }

    static MirrorNetwork forNetwork(ExecutorService executor, List<String> addresses) {
        return new MirrorNetwork(executor, addresses);
    }

    public static MirrorNetwork forMainnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("hcs.mainnet.mirrornode.hedera.com:5600"));
    }

    public static MirrorNetwork forTestnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("hcs.testnet.mirrornode.hedera.com:5600"));
    }

    public static MirrorNetwork forPreviewnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("hcs.previewnet.mirrornode.hedera.com:5600"));
    }

    public boolean isTransportSecurity() {
        return transportSecurity;
    }

    public MirrorNetwork setTransportSecurity(boolean transportSecurity) {
        this.transportSecurityChanged = this.transportSecurity != transportSecurity;
        this.transportSecurity = transportSecurity;
        return this;
    }

    public List<String> getNetwork() {
        var addresses = new ArrayList<String>(nodes.size());

        for (var node : nodes) {
            addresses.add(node.address.toString());
        }

        return addresses;
    }

    public synchronized MirrorNetwork setNetwork(List<String> addresses) throws TimeoutException, InterruptedException {
        lock.acquire();

        var network = new HashSet<>(addresses);
        var stopAt = Instant.now().getEpochSecond() + closeTimeout.getSeconds();

        // Remove nodes that do not exist in new network
        for (int i = 0; i < nodes.size(); i++) {
            var remainingTime = stopAt - Instant.now().getEpochSecond();

            // Exit early if we have no time remaining
            if (remainingTime <= 0) {
                lock.release();
                throw new TimeoutException("Failed to properly shutdown all channels");
            }

            if (!network.contains(nodes.get(i).address.toString())) {
                nodes.get(i).close(Duration.ofSeconds(remainingTime));
                nodes.remove(i);
                i--;
            }
        }

        // Add new nodes that the network doesn't already have
        for (var address : network) {
            if (!this.network.contains(address)) {
                this.nodes.add(new MirrorNode(address, executor));
            }
        }

        this.network = network;
        Collections.shuffle(nodes, ThreadLocalSecureRandom.current());
        lock.release();
        return this;
    }

    private int getNextIndex() {
        var index = this.index;
        this.index = (this.index + 1) % nodes.size();
        return index;
    }

    public MirrorNode getNextMirrorNode() throws InterruptedException {
        if (transportSecurityChanged) {
            lock.acquire();

            var nodes = new ArrayList<MirrorNode>(this.nodes.size());
            var network = new HashSet<String>(this.network.size());

            for (var node : this.nodes) {
                var newNode = transportSecurity ? node.toSecure() : node.toInsecure();

                node.close(closeTimeout);
                nodes.add(newNode);
                network.add(newNode.getAddress().toString());
            }

            this.nodes = nodes;
            this.network = network;

            lock.release();
        }

        return nodes.get(getNextIndex());
    }

    public Duration getCloseTimeout() {
        return closeTimeout;
    }

    public MirrorNetwork setCloseTimeout(Duration closeTimeout) {
        this.closeTimeout = closeTimeout;
        return this;
    }

    public void close() {
        close(closeTimeout);
    }

    public void close(Duration timeout) {
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (var node : nodes) {
            if (node.channel != null) {
                node.channel.shutdown();
            }
        }

        for (var node : nodes) {
            if (node.channel != null) {
                try {
                    while (!node.channel.awaitTermination(timeout.getSeconds(), TimeUnit.SECONDS)) {
                        // Do nothing
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        nodes.clear();
        index = 0;

        lock.release();
    }
}
