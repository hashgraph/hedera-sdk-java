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
