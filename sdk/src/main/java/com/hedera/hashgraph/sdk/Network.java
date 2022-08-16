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

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import com.google.protobuf.ByteString;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

/**
 * Internal utility class.
 */
class Network extends ManagedNetwork<Network, AccountId, Node> {
    @Nullable
    private Integer maxNodesPerRequest;

    /**
     * The protobuf address book converted into a map of node account IDs to NodeAddress
     *
     * This variable is package private so tests can use it
     */
    @Nullable
    Map<AccountId, NodeAddress> addressBook;

    private boolean verifyCertificates = true;

    Network(ExecutorService executor, Map<String, AccountId> network) {
        super(executor);

        try {
            setNetwork(network);
        } catch (InterruptedException | TimeoutException e) {
            // This should never occur. The network is empty.
        }
    }

    /**
     * Create a network.
     *
     * @param executor                  the executor service
     * @param network                   the network records
     * @return                          the new network
     */
    static Network forNetwork(ExecutorService executor, Map<String, AccountId> network) {
        return new Network(executor, network);
    }

    /**
     * Are certificates being verified?
     *
     * @return                          are certificates being verified
     */
    boolean isVerifyCertificates() {
        return verifyCertificates;
    }

    /**
     * Assign the desired verify certificate status.
     *
     * @param verifyCertificates        the desired status
     * @return {@code this}
     */
    Network setVerifyCertificates(boolean verifyCertificates) {
        this.verifyCertificates = verifyCertificates;

        for (var node : nodes) {
            node.setVerifyCertificates(verifyCertificates);
        }

        return this;
    }

    @Override
    synchronized Network setLedgerId(@Nullable LedgerId ledgerId) {
        super.setLedgerId(ledgerId);

        addressBook = (ledgerId == null || !ledgerId.isKnownNetwork()) ? null : readAddressBookResource("addressbook/" + ledgerId + ".pb");
        for (var node : nodes) {
            node.setAddressBook(addressBook == null ? null : addressBook.get(node.getAccountId()));
        }

        return this;
    }

    /**
     * Import an address book.
     *
     * @param fileName                  the file name
     * @return                          the list of address book records
     */
    static Map<AccountId, NodeAddress> readAddressBookResource(String fileName) {
        try (var inputStream = Resources.getResource(fileName).openStream()) {
            var contents = ByteStreams.toByteArray(inputStream);
            var nodeAddressBook = NodeAddressBook.fromBytes(ByteString.copyFrom(contents));
            var map = new HashMap<AccountId, NodeAddress>();

            for (var nodeAddress : nodeAddressBook.nodeAddresses) {
                if (nodeAddress.accountId == null) {
                    continue;
                }

                map.put(nodeAddress.accountId, nodeAddress);
            }

            return map;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extract the of network records.
     *
     * @return                          list of network records
     */
    Map<String, AccountId> getNetwork() {
        Map<String, AccountId> returnMap = new HashMap<>();
        for (var node : nodes) {
            returnMap.put(node.address.toString(), node.getAccountId());
        }
        return returnMap;
    }

    @Override
    protected Node createNodeFromNetworkEntry(Map.Entry<String, AccountId> entry) {
        return new Node(entry.getValue(), entry.getKey(), executor)
            .setVerifyCertificates(verifyCertificates);
    }

    /**
     * Pick 1/3 of the nodes sorted by health and expected delay from the network.
     * This is used by Query and Transaction for selecting node AccountId's.
     *
     * @return {@link java.util.List<com.hedera.hashgraph.sdk.AccountId>}
     */
    synchronized List<AccountId> getNodeAccountIdsForExecute() throws InterruptedException {
        var nodes = getNumberOfMostHealthyNodes(getNumberOfNodesForRequest());
        var nodeAccountIds = new ArrayList<AccountId>(nodes.size());

        for (var node : nodes) {
            nodeAccountIds.add(node.getAccountId());
        }

        return nodeAccountIds;
    }

    /**
     * Assign the maximum nodes to be returned for each request.
     *
     * @param maxNodesPerRequest        the desired number of nodes
     * @return {@code this}
     */
    Network setMaxNodesPerRequest(int maxNodesPerRequest) {
        this.maxNodesPerRequest = maxNodesPerRequest;
        return this;
    }

    /**
     * Extract the number of nodes for each request.
     *
     * @return                          the number of nodes for each request
     */
    int getNumberOfNodesForRequest() {
        if (maxNodesPerRequest != null) {
            return Math.min(maxNodesPerRequest, network.size());
        } else {
            return (network.size() + 3 - 1) / 3;
        }
    }
}
