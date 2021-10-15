package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;

import javax.annotation.Nullable;
import java.util.Objects;

class IPv4Address {
    @Nullable
    IPv4AddressPart network;

    @Nullable
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

    @Nullable
    IPv4AddressPart getNetwork() {
        return network;
    }

    IPv4Address setNetwork(IPv4AddressPart network) {
        this.network = network;
        return this;
    }

    @Nullable
    IPv4AddressPart getHost() {
        return host;
    }

    IPv4Address setHost(IPv4AddressPart host) {
        this.host = host;
        return this;
    }

    ByteString toProtobuf() {
        return ByteString.copyFrom(new byte[]{Objects.requireNonNull(network).left, network.right, Objects.requireNonNull(host).left, host.right});
    }

    @Override
    public String toString() {
        return Objects.requireNonNull(network) + "." + Objects.requireNonNull(host);
    }
}
