package com.hedera.hashgraph.sdk;

import java8.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

class MirrorNetwork extends ManagedNetwork<MirrorNetwork, String, MirrorNode> {
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

    static MirrorNetwork forMainnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("hcs.mainnet.mirrornode.hedera.com:5600"));
    }

    static MirrorNetwork forTestnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("hcs.testnet.mirrornode.hedera.com:5600"));
    }

    static MirrorNetwork forPreviewnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("hcs.previewnet.mirrornode.hedera.com:5600"));
    }

    synchronized MirrorNetwork setNetwork(List<String> network) throws TimeoutException, InterruptedException {
        var map = new HashMap<String, String>(network.size());
        for (var address : network) {
            map.put(address, address);
        }
        return super.setNetwork(map);
    }

    List<String> getNetwork() {
        return Collections.unmodifiableList(new ArrayList<>(network.keySet()));
    }

    @Override
    protected MirrorNode createNodeFromNetworkEntry(Map.Entry<String, String> entry) {
        return new MirrorNode(entry.getKey(), executor).setMinBackoff(minBackoff);
    }

    @Override
    protected List<Integer> getNodesToRemove(Map<String, String> network) {
        var nodes = new ArrayList<Integer>(this.nodes.size());
        var addresses = network.keySet();

        for (int i = this.nodes.size() - 1; i >= 0; i--) {
            var node = this.nodes.get(i);

            if (!addresses.contains(node.getAddress().toString())) {
                nodes.add(i);
            }
        }

        return nodes;
    }

    synchronized MirrorNode getNextMirrorNode() throws InterruptedException {
        return getNumberOfMostHealthyNodes(1).get(0);
    }
}
