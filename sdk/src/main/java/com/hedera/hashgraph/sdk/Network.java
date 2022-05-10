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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

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

    private Network(ExecutorService executor, Map<String, AccountId> network) {
        super(executor);

        try {
            setNetwork(network);
        } catch (InterruptedException | TimeoutException e) {
            // This should never occur. The network is empty.
        }
    }

    static Network forNetwork(ExecutorService executor, Map<String, AccountId> network) {
        return new Network(executor, network);
    }

    static Network forMainnet(ExecutorService executor) {
        var network = new HashMap<String, AccountId>();
        network.put("35.237.200.180:50211", new AccountId(3));
        network.put("34.239.82.6:50211", new AccountId(3));
        network.put("13.82.40.153:50211", new AccountId(3));
        network.put("13.124.142.126:50211", new AccountId(3));
        network.put("15.164.44.66:50211", new AccountId(3));
        network.put("15.165.118.251:50211", new AccountId(3));

        network.put("35.186.191.247:50211", new AccountId(4));
        network.put("3.130.52.236:50211", new AccountId(4));
        network.put("137.116.36.18:50211", new AccountId(4));

        network.put("35.192.2.25:50211", new AccountId(5));
        network.put("3.18.18.254:50211", new AccountId(5));
        network.put("104.43.194.202:50211", new AccountId(5));
        network.put("23.111.186.250:50211", new AccountId(5));
        network.put("74.50.117.35:50211", new AccountId(5));
        network.put("107.155.64.98:50211", new AccountId(5));

        network.put("35.199.161.108:50211", new AccountId(6));
        network.put("13.52.108.243:50211", new AccountId(6));
        network.put("13.64.151.232:50211", new AccountId(6));
        network.put("13.235.15.32:50211", new AccountId(6));
        network.put("104.211.205.124:50211", new AccountId(6));
        network.put("13.71.90.154:50211", new AccountId(6));

        network.put("35.203.82.240:50211", new AccountId(7));
        network.put("3.114.54.4:50211", new AccountId(7));
        network.put("23.102.74.34:50211", new AccountId(7));

        network.put("35.236.5.219:50211", new AccountId(8));
        network.put("35.183.66.150:50211", new AccountId(8));
        network.put("23.96.185.18:50211", new AccountId(8));

        network.put("35.197.192.225:50211", new AccountId(9));
        network.put("35.181.158.250:50211", new AccountId(9));
        network.put("23.97.237.125:50211", new AccountId(9));
        network.put("31.214.8.131:50211", new AccountId(9));

        network.put("35.242.233.154:50211", new AccountId(10));
        network.put("3.248.27.48:50211", new AccountId(10));
        network.put("65.52.68.254:50211", new AccountId(10));
        network.put("179.190.33.184:50211", new AccountId(10));

        network.put("35.240.118.96:50211", new AccountId(11));
        network.put("13.53.119.185:50211", new AccountId(11));
        network.put("23.97.247.27:50211", new AccountId(11));
        network.put("69.87.222.61:50211", new AccountId(11));
        network.put("96.126.72.172:50211", new AccountId(11));
        network.put("69.87.221.231:50211", new AccountId(11));

        network.put("35.204.86.32:50211", new AccountId(12));
        network.put("35.177.162.180:50211", new AccountId(12));
        network.put("51.140.102.228:50211", new AccountId(12));

        network.put("35.234.132.107:50211", new AccountId(13));
        network.put("34.215.192.104:50211", new AccountId(13));
        network.put("13.77.158.252:50211", new AccountId(13));

        network.put("35.236.2.27:50211", new AccountId(14));
        network.put("52.8.21.141:50211", new AccountId(14));
        network.put("40.114.107.85:50211", new AccountId(14));

        network.put("35.228.11.53:50211", new AccountId(15));
        network.put("3.121.238.26:50211", new AccountId(15));
        network.put("40.89.139.247:50211", new AccountId(15));

        network.put("34.91.181.183:50211", new AccountId(16));
        network.put("18.157.223.230:50211", new AccountId(16));
        network.put("13.69.120.73:50211", new AccountId(16));
        network.put("50.7.176.235:50211", new AccountId(16));
        network.put("198.16.99.40:50211", new AccountId(16));
        network.put("50.7.124.46:50211", new AccountId(16));

        network.put("34.86.212.247:50211", new AccountId(17));
        network.put("18.232.251.19:50211", new AccountId(17));
        network.put("40.114.92.39:50211", new AccountId(17));

        network.put("172.105.247.67:50211", new AccountId(18));
        network.put("172.104.150.132:50211", new AccountId(18));
        network.put("139.162.156.222:50211", new AccountId(18));

        network.put("34.89.87.138:50211", new AccountId(19));
        network.put("18.168.4.59:50211", new AccountId(19));
        network.put("51.140.43.81:50211", new AccountId(19));

        network.put("34.82.78.255:50211", new AccountId(20));
        network.put("13.77.151.212:50211", new AccountId(20));

        network.put("34.76.140.109:50211", new AccountId(21));
        network.put("13.36.123.209:50211", new AccountId(21));

        network.put("34.64.141.166:50211", new AccountId(22));
        network.put("52.78.202.34:50211", new AccountId(22));

        network.put("35.232.244.145:50211", new AccountId(23));
        network.put("3.18.91.176:50211", new AccountId(23));

        network.put("34.89.103.38:50211", new AccountId(24));
        network.put("18.135.7.211:50211", new AccountId(24));

        network.put("34.93.112.7:50211", new AccountId(25));
        network.put("13.232.240.207:50211", new AccountId(25));

        network.put("34.87.150.174:50211", new AccountId(26));
        network.put("13.228.103.14:50211", new AccountId(26));

        return new Network(executor, network).setLedgerId(LedgerId.MAINNET);
    }

    static Network forTestnet(ExecutorService executor) {
        var network = new HashMap<String, AccountId>();

        network.put("0.testnet.hedera.com:50211", new AccountId(3));
        network.put("34.94.106.61:50211", new AccountId(3));
        network.put("50.18.132.211:50211", new AccountId(3));
        network.put("138.91.142.219:50211", new AccountId(3));

        network.put("1.testnet.hedera.com:50211", new AccountId(4));
        network.put("35.237.119.55:50211", new AccountId(4));
        network.put("3.212.6.13:50211", new AccountId(4));
        network.put("52.168.76.241:50211", new AccountId(4));

        network.put("2.testnet.hedera.com:50211", new AccountId(5));
        network.put("35.245.27.193:50211", new AccountId(5));
        network.put("52.20.18.86:50211", new AccountId(5));
        network.put("40.79.83.124:50211", new AccountId(5));

        network.put("3.testnet.hedera.com:50211", new AccountId(6));
        network.put("34.83.112.116:50211", new AccountId(6));
        network.put("54.70.192.33:50211", new AccountId(6));
        network.put("52.183.45.65:50211", new AccountId(6));

        network.put("4.testnet.hedera.com:50211", new AccountId(7));
        network.put("34.94.160.4:50211", new AccountId(7));
        network.put("54.176.199.109:50211", new AccountId(7));
        network.put("13.64.181.136:50211", new AccountId(7));

        network.put("5.testnet.hedera.com:50211", new AccountId(8));
        network.put("34.106.102.218:50211", new AccountId(8));
        network.put("35.155.49.147:50211", new AccountId(8));
        network.put("13.78.238.32:50211", new AccountId(8));

        network.put("6.testnet.hedera.com:50211", new AccountId(9));
        network.put("34.133.197.230:50211", new AccountId(9));
        network.put("52.14.252.207:50211", new AccountId(9));
        network.put("52.165.17.231:50211", new AccountId(9));

        return new Network(executor, network).setLedgerId(LedgerId.TESTNET);
    }

    static Network forPreviewnet(ExecutorService executor) {
        var network = new HashMap<String, AccountId>();
        network.put("0.previewnet.hedera.com:50211", new AccountId(3));
        network.put("35.231.208.148:50211", new AccountId(3));
        network.put("3.211.248.172:50211", new AccountId(3));
        network.put("40.121.64.48:50211", new AccountId(3));

        network.put("1.previewnet.hedera.com:50211", new AccountId(4));
        network.put("35.199.15.177:50211", new AccountId(4));
        network.put("3.133.213.146:50211", new AccountId(4));
        network.put("40.70.11.202:50211", new AccountId(4));

        network.put("2.previewnet.hedera.com:50211", new AccountId(5));
        network.put("35.225.201.195:50211", new AccountId(5));
        network.put("52.15.105.130:50211", new AccountId(5));
        network.put("104.43.248.63:50211", new AccountId(5));

        network.put("3.previewnet.hedera.com:50211", new AccountId(6));
        network.put("35.247.109.135:50211", new AccountId(6));
        network.put("54.241.38.1:50211", new AccountId(6));
        network.put("13.88.22.47:50211", new AccountId(6));

        network.put("4.previewnet.hedera.com:50211", new AccountId(7));
        network.put("35.235.65.51:50211", new AccountId(7));
        network.put("54.177.51.127:50211", new AccountId(7));
        network.put("13.64.170.40:50211", new AccountId(7));

        network.put("5.previewnet.hedera.com:50211", new AccountId(8));
        network.put("34.106.247.65:50211", new AccountId(8));
        network.put("35.83.89.171:50211", new AccountId(8));
        network.put("13.78.232.192:50211", new AccountId(8));

        network.put("6.previewnet.hedera.com:50211", new AccountId(9));
        network.put("34.125.23.49:50211", new AccountId(9));
        network.put("50.18.17.93:50211", new AccountId(9));
        network.put("20.150.136.89:50211", new AccountId(9));

        return new Network(executor, network).setLedgerId(LedgerId.PREVIEWNET);
    }

    boolean isVerifyCertificates() {
        return verifyCertificates;
    }

    synchronized Network setVerifyCertificates(boolean verifyCertificates) {
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

    synchronized Map<String, AccountId> getNetwork() {
        Map<String, AccountId> returnMap = new HashMap<>();
        for (var node : nodes) {
            returnMap.put(node.address.toString(), node.getAccountId());
        }
        return returnMap;
    }

    @Override
    synchronized protected Node createNodeFromNetworkEntry(Map.Entry<String, AccountId> entry) {
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

    Network setMaxNodesPerRequest(int maxNodesPerRequest) {
        this.maxNodesPerRequest = maxNodesPerRequest;
        return this;
    }

    int getNumberOfNodesForRequest() {
        if (maxNodesPerRequest != null) {
            return Math.min(maxNodesPerRequest, network.size());
        } else {
            return (network.size() + 3 - 1) / 3;
        }
    }
}
