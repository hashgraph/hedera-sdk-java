package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.NetworkGetVersionInfoResponse;

public class NetworkVersionInfo {
    /**
     * Version of the protobuf schema in use by the network
     */
    public final SemanticVersion protobufVersion;

    /**
     * Version of the Hedera services in use by the network
     */
    public final SemanticVersion servicesVersion;

    NetworkVersionInfo(SemanticVersion hapi, SemanticVersion hedera) {
        this.protobufVersion = hapi;
        this.servicesVersion = hedera;
    }

    protected static NetworkVersionInfo fromProtobuf(NetworkGetVersionInfoResponse proto) {
        return new NetworkVersionInfo(
            SemanticVersion.fromProtobuf(proto.getHapiProtoVersion()),
            SemanticVersion.fromProtobuf(proto.getHederaServicesVersion())
        );
    }

    public static NetworkVersionInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(NetworkGetVersionInfoResponse.parseFrom(bytes));
    }

    protected NetworkGetVersionInfoResponse toProtobuf() {
        return NetworkGetVersionInfoResponse.newBuilder()
            .setHapiProtoVersion(protobufVersion.toProtobuf())
            .setHederaServicesVersion(servicesVersion.toProtobuf())
            .build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
