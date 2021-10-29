package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import java8.util.Lists;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

class MirrorNetwork {
    final ExecutorService executor;
    final Semaphore lock = new Semaphore(1);
    List<String> addresses = new CopyOnWriteArrayList<>();
    List<MirrorNode> network = new ArrayList<>();
    int index = 0;

    MirrorNetwork(ExecutorService executor) {
        this.executor = executor;

        try {
            setNetwork(addresses);
        } catch (InterruptedException e) {
            // Do nothing as this should never occur
        }
    }

    static MirrorNetwork forNetwork(ExecutorService executor, List<String> addresses) {
        try {
            return new MirrorNetwork(executor).setNetwork(addresses);
        } catch (InterruptedException e) {
            // Should never happen
            throw new RuntimeException(e);
        }
    }

    static MirrorNetwork forMainnet(ExecutorService executor) {
        return MirrorNetwork.forNetwork(executor, Lists.of("hcs.mainnet.mirrornode.hedera.com:5600"));
    }

    static MirrorNetwork forTestnet(ExecutorService executor) {
        return MirrorNetwork.forNetwork(executor, Lists.of("hcs.testnet.mirrornode.hedera.com:5600"));
    }

    static MirrorNetwork forPreviewnet(ExecutorService executor) {
        return MirrorNetwork.forNetwork(executor, Lists.of("hcs.previewnet.mirrornode.hedera.com:5600"));
    }

    List<String> getNetwork() {
        return Collections.unmodifiableList(addresses);
    }

    synchronized MirrorNetwork setNetwork(List<String> addresses) throws InterruptedException {
        lock.acquire();

        var stopAt = Instant.now().getEpochSecond() + Duration.ofSeconds(30).getSeconds();

        // Remove nodes that do not exist in new network
        for (int i = 0; i < network.size(); i++) {
            if (!addresses.contains(network.get(i).address.toString())) {
                network.get(i).close(stopAt - Instant.now().getEpochSecond());
                network.remove(i);
                i--;
            }
        }

        // Add new nodes that the network doesn't already have
        for (var address : addresses) {
            @Var
            var contains = false;
            for (var node : this.network) {
                if (node.address.toString().equals(address)) {
                    contains = true;
                }
            }

            if (!contains) {
                this.network.add(new MirrorNode(address, executor));
            }
        }

        this.addresses = new CopyOnWriteArrayList<String>(addresses);
        Collections.shuffle(network, ThreadLocalSecureRandom.current());

        lock.release();
        return this;
    }

    MirrorNode getNextMirrorNode() {
        var node = network.get(index);
        index = (index + 1) % network.size();
        return node;
    }

    void close(Duration timeout) {
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (var node : network) {
            if (node.channel != null) {
                node.channel.shutdown();
            }
        }

        for (var node : network) {
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

        network.clear();
        addresses.clear();
        index = 0;

        lock.release();
    }
}
