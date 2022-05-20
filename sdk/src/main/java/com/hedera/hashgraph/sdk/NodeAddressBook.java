package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A list of nodes and their metadata.
 *
 * {@link https://docs.hedera.com/guides/docs/hedera-api/basic-types/nodeaddressbook}
 */
public class NodeAddressBook {
    List<NodeAddress> nodeAddresses = Collections.emptyList();

    /**
     * Constructor.
     */
    NodeAddressBook() {
    }

    /**
     * @return                          list of node addresses
     */
    public List<NodeAddress> getNodeAddresses() {
        return nodeAddresses;
    }

    /**
     * Assign the list of node addresses.
     *
     * @param nodeAddresses             list of node addresses
     * @return {@code this}
     */
    public NodeAddressBook setNodeAddresses(List<NodeAddress> nodeAddresses) {
        this.nodeAddresses = nodeAddresses;
        return this;
    }

    /**
     * Create a node address book from a protobuf.
     *
     * @param book                      the protobuf
     * @return                          the new node address book
     */
    static NodeAddressBook fromProtobuf(com.hedera.hashgraph.sdk.proto.NodeAddressBook book) {
        var addresses = new ArrayList<NodeAddress>(book.getNodeAddressCount());

        for (var address : book.getNodeAddressList()) {
            addresses.add(NodeAddress.fromProtobuf(address));
        }

        return new NodeAddressBook().setNodeAddresses(addresses);
    }

    /**
     * Create a node address book from a byte string.
     *
     * @param bytes                     the byte string
     * @return                          the new node address book
     * @throws InvalidProtocolBufferException
     */
    public static NodeAddressBook fromBytes(ByteString bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.NodeAddressBook.parseFrom(bytes));
    }

    /**
     * @return                          the protobuf representation
     */
    com.hedera.hashgraph.sdk.proto.NodeAddressBook toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.NodeAddressBook.newBuilder();

        for (var nodeAdress : nodeAddresses) {
            builder.addNodeAddress(nodeAdress.toProtobuf());
        }

        return builder.build();
    }

    /**
     * @return                          the byte string representation
     */
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
