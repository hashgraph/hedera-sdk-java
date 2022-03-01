package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeAddressBook {
    List<NodeAddress> nodeAddresses = Collections.emptyList();

    NodeAddressBook() {
    }

    public List<NodeAddress> getNodeAddresses() {
        return nodeAddresses;
    }

    public NodeAddressBook setNodeAddresses(List<NodeAddress> nodeAddresses) {
        this.nodeAddresses = nodeAddresses;
        return this;
    }

    static NodeAddressBook fromProtobuf(com.hedera.hashgraph.sdk.proto.NodeAddressBook book) {
        var addresses = new ArrayList<NodeAddress>(book.getNodeAddressCount());

        for (var address : book.getNodeAddressList()) {
            addresses.add(NodeAddress.fromProtobuf(address));
        }

        return new NodeAddressBook().setNodeAddresses(addresses);
    }

    public static NodeAddressBook fromBytes(ByteString bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.NodeAddressBook.parseFrom(bytes));
    }

    com.hedera.hashgraph.sdk.proto.NodeAddressBook toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.NodeAddressBook.newBuilder();

        for (var nodeAdress : nodeAddresses) {
            builder.addNodeAddress(nodeAdress.toProtobuf());
        }

        return builder.build();
    }

    public ByteString toBytes() {
        return toProtobuf().toByteString();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("nodeAddresses", nodeAddresses)
            .toString();
    }
}
