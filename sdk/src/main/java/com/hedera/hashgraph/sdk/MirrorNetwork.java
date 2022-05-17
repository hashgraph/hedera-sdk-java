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

import java8.util.Lists;

import java.util.ArrayList;
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
        return new MirrorNetwork(executor, Lists.of("hcs.mainnet.mirrornode.hedera.com:5600"));
    }

    static MirrorNetwork forTestnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("hcs.testnet.mirrornode.hedera.com:5600"));
    }

    static MirrorNetwork forPreviewnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("hcs.previewnet.mirrornode.hedera.com:5600"));
    }

    synchronized MirrorNetwork setNetwork(List<String> network) throws TimeoutException, InterruptedException {
        var map = new HashMap<String, ManagedNodeAddress>(network.size());
        for (var address : network) {
            map.put(address, ManagedNodeAddress.fromString(address));
        }
        return super.setNetwork(map);
    }

    synchronized List<String> getNetwork() {
        List<String> retval = new ArrayList<>(network.size());
        for (var address : network.keySet()) {
            retval.add(address.toString());
        }
        return retval;
    }

    @Override
    protected synchronized MirrorNode createNodeFromNetworkEntry(Map.Entry<String, ManagedNodeAddress> entry) {
        return new MirrorNode(entry.getKey(), executor);
    }

    synchronized MirrorNode getNextMirrorNode() throws InterruptedException {
        return getNumberOfMostHealthyNodes(1).get(0);
    }
}
