package com.hedera.hashgraph.sdk;

import com.google.common.collect.HashBiMap;
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
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

class Network extends ManagedNetwork<Network, AccountId, Node, Map<String, AccountId>, Map.Entry<String, AccountId>> {
    @Nullable
    private Integer maxNodesPerRequest;

    @Nullable
    private Map<AccountId, NodeAddress> addressBook;

    private boolean verifyCertificates = true;

    private Network(ExecutorService executor, Map<String, AccountId> network) {
        super(executor);

        try {
            setNetwork(network);
        } catch (InterruptedException | TimeoutException e) {
            // This should never occur. The network is empty.
        }
    }

    public static Network forNetwork(ExecutorService executor, Map<String, AccountId> network) {
        return new Network(executor, network);
    }

    public static Network forMainnet(ExecutorService executor) {
        var network = new HashMap<String, AccountId>();
        network.put("35.237.200.180:50211", new AccountId(3));
        network.put("35.186.191.247:50211", new AccountId(4));
        network.put("35.192.2.25:50211", new AccountId(5));
        network.put("35.199.161.108:50211", new AccountId(6));
        network.put("35.203.82.240:50211", new AccountId(7));
        network.put("35.236.5.219:50211", new AccountId(8));
        network.put("35.197.192.225:50211", new AccountId(9));
        network.put("35.242.233.154:50211", new AccountId(10));
        network.put("35.240.118.96:50211", new AccountId(11));
        network.put("35.204.86.32:50211", new AccountId(12));
        network.put("35.234.132.107:50211", new AccountId(13));
        network.put("35.236.2.27:50211", new AccountId(14));
        network.put("35.228.11.53:50211", new AccountId(15));
        network.put("34.91.181.183:50211", new AccountId(16));
        network.put("34.86.212.247:50211", new AccountId(17));
        network.put("172.105.247.67:50211", new AccountId(18));
        network.put("34.89.87.138:50211", new AccountId(19));
        network.put("34.82.78.255:50211", new AccountId(20));

        return new Network(executor, network).setNetworkName(NetworkName.MAINNET);
    }

    public static Network forTestnet(ExecutorService executor) {
        var network = new HashMap<String, AccountId>();
        network.put("0.testnet.hedera.com:50211", new AccountId(3));
        network.put("1.testnet.hedera.com:50211", new AccountId(4));
        network.put("2.testnet.hedera.com:50211", new AccountId(5));
        network.put("3.testnet.hedera.com:50211", new AccountId(6));
        network.put("4.testnet.hedera.com:50211", new AccountId(7));

        return new Network(executor, network).setNetworkName(NetworkName.TESTNET);
    }

    public static Network forPreviewnet(ExecutorService executor) {
        var network = new HashMap<String, AccountId>();
        network.put("0.previewnet.hedera.com:50211", new AccountId(3));
        network.put("1.previewnet.hedera.com:50211", new AccountId(4));
        network.put("2.previewnet.hedera.com:50211", new AccountId(5));
        network.put("3.previewnet.hedera.com:50211", new AccountId(6));
        network.put("4.previewnet.hedera.com:50211", new AccountId(7));

        return new Network(executor, network).setNetworkName(NetworkName.PREVIEWNET);
    }

    public boolean isVerifyCertificates() {
        return verifyCertificates;
    }

    public Network setVerifyCertificates(boolean verifyCertificates) {
        this.verifyCertificates = verifyCertificates;

        for (var node : nodes) {
            node.setVerifyCertificates(verifyCertificates);
        }

        return this;
    }

    public Network setNetworkName(@Nullable NetworkName networkName) {
        super.setNetworkName(networkName);

        addressBook = networkName == null ? null : readAddressBookResource("addressbook/" + networkName + ".pb");
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

    public Map<String, AccountId> getNetwork() {
        var network = new HashMap<String, AccountId>(this.nodes.size());

        for (var node : nodes) {
            network.put(node.address.toString(), node.getAccountId());
        }

        return network;
    }

    @Override
    protected Iterable<Map.Entry<String, AccountId>> createIterableNetwork(Map<String, AccountId> network) {
        return network.entrySet();
    }

    @Override
    protected Node createNodeFromNetworkEntry(Map.Entry<String, AccountId> entry) {
        return new Node(entry.getValue(), entry.getKey(), executor)
            .setMinBackoff(minBackoff)
            .setVerifyCertificates(verifyCertificates);
    }

    @Override
    protected void addNodeToNetwork(Node node) {
        network.put(node.getAccountId(), node);
    }

    @Override
    protected void removeNodeFromNetwork(Node node) {
        network.remove(node.getAccountId());
    }

    @Override
    protected List<Integer> getNodesToRemove(Map<String, AccountId> network) {
        var nodes = new ArrayList<Integer>(this.nodes.size());
        var newNodeAccountIds = network.values();
        var inverted = HashBiMap.create(network).inverse();

        for (int i = this.nodes.size() - 1; i >= 0; i--) {
            var node = this.nodes.get(i);

            if (
                !newNodeAccountIds.contains(node.getAccountId()) ||
                    !Objects.requireNonNull(inverted.get(node.getAccountId())).equals(node.getAddress().toString())
            ) {
                nodes.add(i);
            }
        }

        return nodes;
    }

    @Override
    protected boolean checkNetworkContainsEntry(Map.Entry<String, AccountId> entry) {
        return this.network.containsKey(entry.getValue());
    }

    /**
     * Pick 1/3 of the nodes sorted by health and expected delay from the network.
     * This is used by Query and Transaction for selecting node AccountId's.
     *
     * @return {@link java.util.List<com.hedera.hashgraph.sdk.AccountId>}
     */
    public List<AccountId> getNodeAccountIdsForExecute() throws InterruptedException {
        var nodes = getNumberOfMostHealthyNodes(getNumberOfNodesForRequest());
        var nodeAccountIds = new ArrayList<AccountId>(nodes.size());

        for (var node : nodes) {
            nodeAccountIds.add(node.getAccountId());
        }

        return nodeAccountIds;
    }

    public Network setMaxNodesPerRequest(int maxNodesPerRequest) {
        this.maxNodesPerRequest = maxNodesPerRequest;
        return this;
    }

    public int getNumberOfNodesForRequest() {
        if (maxNodesPerRequest != null) {
            return Math.min(maxNodesPerRequest, nodes.size());
        } else {
            return (nodes.size() + 3 - 1) / 3;
        }
    }

    public Node getNodeForAccountId(AccountId nodeAccountId) {
        return network.get(nodeAccountId);
    }
}
