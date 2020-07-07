package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;

public class SemanticVersion {
    public int major;
    public int minor;
    public int patch;

    SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    protected static SemanticVersion fromProtobuf(com.hedera.hashgraph.sdk.proto.SemanticVersion version) {
        return new SemanticVersion(
            version.getMajor(),
            version.getMinor(),
            version.getPatch()
        );
    }

    protected com.hedera.hashgraph.sdk.proto.SemanticVersion toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.SemanticVersion.newBuilder()
            .setMajor(major)
            .setMinor(minor)
            .setPatch(patch)
            .build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    public static SemanticVersion fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.SemanticVersion.parseFrom(bytes));
    }
}
