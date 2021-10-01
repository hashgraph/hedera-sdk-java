package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ServiceEndpoint;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class NodeAddress {
    @Nullable
    String publicKey;

    @Nullable
    AccountId accountId;

    long nodeId;

    @Nullable
    ByteString certHash;

    List<Endpoint> addresses = Collections.emptyList();

    @Nullable
    String description = null;

    long stake;

    NodeAddress() {
    }

    static NodeAddress fromProtobuf(com.hedera.hashgraph.sdk.proto.NodeAddress nodeAddress) {
        var address = new ArrayList<Endpoint>(nodeAddress.getServiceEndpointCount());

        if (!nodeAddress.getIpAddress().isEmpty()) {
            address.add(
                Endpoint.fromProtobuf(ServiceEndpoint.newBuilder()
                    .setIpAddressV4(nodeAddress.getIpAddress())
                    .setPort(nodeAddress.getPortno())
                    .build())
            );
        }

        for (var endpoint : nodeAddress.getServiceEndpointList()) {
            address.add(Endpoint.fromProtobuf(endpoint));
        }

        var node = new NodeAddress()
            .setPublicKey(nodeAddress.getRSAPubKey())
            .setNodeId(nodeAddress.getNodeId())
            .setCertHash(nodeAddress.getNodeCertHash())
            .setAddresses(address)
            .setDescription(nodeAddress.getDescription())
            .setStake(nodeAddress.getStake());

        if (nodeAddress.hasNodeAccountId()) {
            node.setAccountId(AccountId.fromProtobuf(nodeAddress.getNodeAccountId()));
        }

        return node;
    }

    @Nullable
    String getPublicKey() {
        return publicKey;
    }

    NodeAddress setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    @Nullable
    AccountId getAccountId() {
        return accountId;
    }

    NodeAddress setAccountId(AccountId accountId) {
        this.accountId = accountId;
        return this;
    }

    long getNodeId() {
        return nodeId;
    }

    NodeAddress setNodeId(long nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    @Nullable
    ByteString getCertHash() {
        return certHash;
    }

    NodeAddress setCertHash(ByteString certHash) {
        this.certHash = certHash;
        return this;
    }

    List<Endpoint> getAddresses() {
        return addresses;
    }

    NodeAddress setAddresses(List<Endpoint> addresses) {
        this.addresses = addresses;
        return this;
    }

    @Nullable
    String getDescription() {
        return description;
    }

    NodeAddress setDescription(String description) {
        this.description = description;
        return this;
    }

    long getStake() {
        return stake;
    }

    NodeAddress setStake(long stake) {
        this.stake = stake;
        return this;
    }

    com.hedera.hashgraph.sdk.proto.NodeAddress toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.NodeAddress.newBuilder()
            .setNodeId(nodeId);

        if (certHash != null) {
            builder.setNodeCertHash(certHash);
        }

        if (publicKey != null) {
            builder.setRSAPubKey(publicKey);
        }

        if (accountId != null) {
            builder.setNodeAccountId(accountId.toProtobuf());
        }

        if (description != null) {
            builder.setDescription(description);
        }

        for (var address : addresses) {
            builder.addServiceEndpoint(address.toProtobuf());
        }

        return builder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("publicKey", publicKey)
            .add("accountId", accountId)
            .add("nodeId", nodeId)
            .add("certHash", certHash != null ? new String(certHash.toByteArray(), StandardCharsets.UTF_8) : null)
            .add("addresses", addresses)
            .add("description", description)
            .add("stake", stake)
            .toString();
    }
}
