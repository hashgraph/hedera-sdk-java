package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.NetworkGetVersionInfoResponse;

public class NetworkVersionInfo {
    public SemanticVersion hapiProtoVersion;
    public SemanticVersion hederaServicesVersion;

    NetworkVersionInfo(SemanticVersion hapi, SemanticVersion hedera) {
        this.hapiProtoVersion = hapi;
        this.hederaServicesVersion = hedera;
    }

    protected static NetworkVersionInfo fromProtobuf(NetworkGetVersionInfoResponse proto) {
        return new NetworkVersionInfo(
            SemanticVersion.fromProtobuf(proto.getHapiProtoVersion()),
            SemanticVersion.fromProtobuf(proto.getHederaServicesVersion())
        );
    }

    protected NetworkGetVersionInfoResponse toProtobuf() {
        return NetworkGetVersionInfoResponse.newBuilder()
            .setHapiProtoVersion(hapiProtoVersion.toProtobuf())
            .setHederaServicesVersion(hederaServicesVersion.toProtobuf())
            .build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    public static NetworkVersionInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(NetworkGetVersionInfoResponse.parseFrom(bytes));
    }
}
