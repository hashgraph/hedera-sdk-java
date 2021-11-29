package com.hedera.hashgraph.sdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

class MirrorNetwork extends ManagedNetwork<MirrorNetwork, ManagedNodeAddress, MirrorNode> {
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
        return MirrorNetwork.forNetwork(executor, List.of("hcs.mainnet.mirrornode.hedera.com:5600"));
    }

    static MirrorNetwork forTestnet(ExecutorService executor) {
        return MirrorNetwork.forNetwork(executor, List.of("hcs.testnet.mirrornode.hedera.com:5600"));
    }

    static MirrorNetwork forPreviewnet(ExecutorService executor) {
        return MirrorNetwork.forNetwork(executor, List.of("hcs.previewnet.mirrornode.hedera.com:5600"));
    }

    synchronized MirrorNetwork setNetwork(List<String> network) throws TimeoutException, InterruptedException {
        var map = new HashMap<String, ManagedNodeAddress>(network.size());
        for (var address : network) {
            map.put(address, ManagedNodeAddress.fromString(address));
        }
        return super.setNetwork(map);
    }

    List<String> getNetwork() {
        List<String> retval = new ArrayList<>(network.size());
        for (var address : network.keySet()) {
            retval.add(address.toString());
        }
        return retval;
    }

    @Override
    protected MirrorNode createNodeFromNetworkEntry(Map.Entry<String, ManagedNodeAddress> entry) {
        return new MirrorNode(entry.getKey(), executor).setMinBackoff(minBackoff);
    }

    synchronized MirrorNode getNextMirrorNode() throws InterruptedException {
        return getNumberOfMostHealthyNodes(1).get(0);
    }
}
