package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A simplified representation of a 32 bit IPv4Address.
 */
public class IPv4Address {
    /**
     * Represents the first 16 bits of the IPv4Address.
     */
    @Nullable
    IPv4AddressPart network;
    /**
     * Represents the last 16 bits of the IPv4Address.
     */
    @Nullable
    IPv4AddressPart host;

    /**
     * Constructor.
     */
    IPv4Address() {
    }

    /**
     * Create an ipv4address from a protobuf byte string.
     *
     * @param address                   the byte string
     * @return                          the new ipv4address
     */
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

    /**
     * @return                          the network portion of the address
     */
    @Nullable
    public IPv4AddressPart getNetwork() {
        return network;
    }

    /**
     * Assign the network portion of the address.
     *
     * @param network                   the network portion of the address
     * @return {@code this}
     */
    public IPv4Address setNetwork(IPv4AddressPart network) {
        this.network = network;
        return this;
    }

    /**
     * @return                          the host portion of the address
     */
    @Nullable
    public IPv4AddressPart getHost() {
        return host;
    }

    /**
     * Assign the host portion of the address.
     *
     * @param host                      the host portion of the address
     * @return {@code this}
     */
    public IPv4Address setHost(IPv4AddressPart host) {
        this.host = host;
        return this;
    }

    /**
     * @return                          the protobuf representation
     */
    ByteString toProtobuf() {
        return ByteString.copyFrom(new byte[]{Objects.requireNonNull(network).left, network.right, Objects.requireNonNull(host).left, host.right});
    }

    /**
     * @return                          the string representation
     */
    @Override
    public String toString() {
        return Objects.requireNonNull(network) + "." + Objects.requireNonNull(host);
    }
}
