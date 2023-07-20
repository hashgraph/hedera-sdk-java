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

/**
 * Utility class.
 */
class MirrorNetwork extends BaseNetwork<MirrorNetwork, BaseNodeAddress, MirrorNode> {
    private MirrorNetwork(ExecutorService executor, List<String> addresses) {
        super(executor);
        this.transportSecurity = true;
        try {
            setNetwork(addresses);
        } catch (InterruptedException | TimeoutException e) {
            // This should never occur. The network is empty.
        }
    }

    /**
     * Create an arbitrary mirror network.
     *
     * @param executor                  the executor service
     * @param addresses                 the arbitrary address for the network
     * @return                          the new mirror network object
     */
    static MirrorNetwork forNetwork(ExecutorService executor, List<String> addresses) {
        return new MirrorNetwork(executor, addresses);
    }

    /**
     * Create a mirror network for mainnet.
     *
     * @param executor                  the executor service
     * @return                          the new mirror network for mainnet
     */
    static MirrorNetwork forMainnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("mainnet-public.mirrornode.hedera.com:443"));
    }

    /**
     * Create a mirror network for testnet.
     *
     * @param executor                  the executor service
     * @return                          the new mirror network for testnet
     */
    static MirrorNetwork forTestnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("testnet.mirrornode.hedera.com:443"));
    }

    /**
     * Create a mirror network for previewnet.
     *
     * @param executor                  the executor service
     * @return                          the new mirror network for previewnet
     */
    static MirrorNetwork forPreviewnet(ExecutorService executor) {
        return new MirrorNetwork(executor, Lists.of("previewnet.mirrornode.hedera.com:443"));
    }

    /**
     * Assign the desired network.
     *
     * @param network                   the desired network
     * @return                          the mirror network
     * @throws TimeoutException         when the transaction times out
     * @throws InterruptedException     when a thread is interrupted while it's waiting, sleeping, or otherwise occupied
     */
    synchronized MirrorNetwork setNetwork(List<String> network) throws TimeoutException, InterruptedException {
        var map = new HashMap<String, BaseNodeAddress>(network.size());
        for (var address : network) {
            map.put(address, BaseNodeAddress.fromString(address));
        }
        return super.setNetwork(map);
    }

    /**
     * Extract the network names.
     *
     * @return                          the network names
     */
    synchronized List<String> getNetwork() {
        List<String> retval = new ArrayList<>(network.size());
        for (var address : network.keySet()) {
            retval.add(address.toString());
        }
        return retval;
    }

    @Override
    protected MirrorNode createNodeFromNetworkEntry(Map.Entry<String, BaseNodeAddress> entry) {
        return new MirrorNode(entry.getKey(), executor);
    }

    /**
     * Extract the next healthy mirror node on the list.
     *
     * @return                          the next healthy mirror node on the list
     * @throws InterruptedException     when a thread is interrupted while it's waiting, sleeping, or otherwise occupied
     */
    synchronized MirrorNode getNextMirrorNode() throws InterruptedException {
        return getNumberOfMostHealthyNodes(1).get(0);
    }
}
