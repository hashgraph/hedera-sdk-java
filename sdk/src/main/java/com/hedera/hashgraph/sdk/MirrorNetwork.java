package com.hedera.hashgraph.sdk;

import java8.util.Lists;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

class MirrorNetwork extends ManagedNetwork<MirrorNetwork, String, MirrorNode, List<String>, String> {
    private HashSet<String> network = new HashSet<>();

    private MirrorNetwork(ExecutorService executor, List<String> addresses) {
        super(executor);

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

    public List<String> getNetwork() {
        var addresses = new ArrayList<String>(nodes.size());

        for (var node : nodes) {
            addresses.add(node.address.toString());
        }

        return addresses;
    }

    @Override
    protected Iterable<String> createIterableNetwork(List<String> network) {
        return network;
    }

    @Override
    protected MirrorNode createNodeFromNetworkEntry(String entry) {
        return new MirrorNode(entry, executor).setMinBackoff(minBackoff);
    }

    @Override
    protected void addNodeToNetwork(MirrorNode node) {
        this.network.add(node.getAddress().toString());
    }

    @Override
    protected void removeNodeFromNetwork(MirrorNode node) {
        this.network.remove(node.getAddress().toString());
    }

    @Override
    protected List<Integer> getNodesToRemove(List<String> network) {
        var nodes = new ArrayList<Integer>(this.nodes.size());

        for (int i = this.nodes.size() - 1; i >= 0; i--) {
            var node = this.nodes.get(i);

            if (!network.contains(node.getAddress().toString())) {
                nodes.add(i);
            }
        }

        return nodes;
    }

    @Override
    protected boolean checkNetworkContainsEntry(String entry) {
        for (var address : network) {
            if (address.equals(entry)) {
                return true;
            }
        }

        return false;
    }

    public MirrorNode getNextMirrorNode() throws InterruptedException {
        return getNumberOfMostHealthyNodes(1).get(0);
    }
}
