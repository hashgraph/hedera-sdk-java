package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;

class IPv4Address {
    IPv4AddressPart network;
    IPv4AddressPart host;

    IPv4Address() {
    }

    static IPv4Address fromProtobuf(ByteString address) {
        return new IPv4Address()
            .setNetwork(new IPv4AddressPart()
                .setLeft(address.byteAt(0))
                .setRight(address.byteAt(1))
            )
            .setHost(new IPv4AddressPart()
                .setLeft(address.byteAt(2))
                .setRight(address.byteAt(3))
            );
    }

    IPv4AddressPart getNetwork() {
        return network;
    }

    IPv4Address setNetwork(IPv4AddressPart network) {
        this.network = network;
        return this;
    }

    IPv4AddressPart getHost() {
        return host;
    }

    IPv4Address setHost(IPv4AddressPart host) {
        this.host = host;
        return this;
    }

    ByteString toProtobuf() {
        return ByteString.copyFrom(new byte[]{network.left, network.right, host.left, host.right});
    }

    public String toString() {
        return network.toString() + "." + host.toString();
    }
}
