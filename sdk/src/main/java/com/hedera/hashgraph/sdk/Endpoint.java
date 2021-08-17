package com.hedera.hashgraph.sdk;

class Endpoint {
    IPv4Address address;
    int port;

    Endpoint() {
    }

    static Endpoint fromProtobuf(com.hedera.hashgraph.sdk.proto.ServiceEndpoint serviceEndpoint) {
        return new Endpoint()
            .setAddress(IPv4Address.fromProtobuf(serviceEndpoint.getIpAddressV4()))
            .setPort((int) Integer.toUnsignedLong(serviceEndpoint.getPort()));
    }

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

    com.hedera.hashgraph.sdk.proto.ServiceEndpoint toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.ServiceEndpoint.newBuilder();

        if (address != null) {
            builder.setIpAddressV4(address.toProtobuf());
        }

        return builder.setPort(port).build();
    }

    public String toString() {
        return address.toString() +
            ":" +
            port;
    }
}
