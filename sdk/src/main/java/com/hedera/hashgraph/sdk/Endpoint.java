package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.proto.ServiceEndpoint;

import javax.annotation.Nullable;
import java.util.Objects;

class Endpoint {
    @Nullable
    IPv4Address address = null;

    int port;

    Endpoint() {
    }

    static Endpoint fromProtobuf(ServiceEndpoint serviceEndpoint) {
        @Var var port = (int) (serviceEndpoint.getPort() & 0x00000000ffffffffL);

        if (port == 0 || port == 50111) {
            port = 50211;
        }

        return new Endpoint()
            .setAddress(IPv4Address.fromProtobuf(serviceEndpoint.getIpAddressV4()))
            .setPort(port);
    }

    @Nullable
    IPv4Address getAddress() {
        return address;
    }

    Endpoint setAddress(IPv4Address address) {
        this.address = address;
        return this;
    }

    int getPort() {
        return port;
    }

    Endpoint setPort(int port) {
        this.port = port;
        return this;
    }

    ServiceEndpoint toProtobuf() {
        var builder = ServiceEndpoint.newBuilder();

        if (address != null) {
            builder.setIpAddressV4(address.toProtobuf());
        }

        return builder.setPort(port).build();
    }

    @Override
    public String toString() {
        return Objects.requireNonNull(address) +
            ":" +
            port;
    }
}
