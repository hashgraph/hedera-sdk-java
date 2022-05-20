package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Hedera follows semantic versioning () for both the HAPI protobufs and
 * the Services software. This type allows the getVersionInfo query in the
 * NetworkService to return the deployed versions of both protobufs and
 * software on the node answering the query.
 *
 * {@link https://docs.hedera.com/guides/docs/hedera-api/basic-types/semanticversion}
 */
public class SemanticVersion {
    /**
     * Increases with incompatible API changes
     */
    public int major;
    /**
     * Increases with backwards-compatible new functionality
     */
    public int minor;
    /**
     * Increases with backwards-compatible bug fixes
     */
    public int patch;

    /**
     * Constructor.
     *
     * @param major                     the major part
     * @param minor                     the minor part
     * @param patch                     the patch part
     */
    SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Create a semantic version object from a protobuf.
     *
     * @param version                   the protobuf
     * @return                          the new semantic version
     */
    protected static SemanticVersion fromProtobuf(com.hedera.hashgraph.sdk.proto.SemanticVersion version) {
        return new SemanticVersion(
            version.getMajor(),
            version.getMinor(),
            version.getPatch()
        );
    }

    /**
     * Create a semantic version from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new semantic version
     * @throws InvalidProtocolBufferException
     */
    public static SemanticVersion fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.SemanticVersion.parseFrom(bytes));
    }

    /**
     * @return                          the protobuf representation
     */
    protected com.hedera.hashgraph.sdk.proto.SemanticVersion toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.SemanticVersion.newBuilder()
            .setMajor(major)
            .setMinor(minor)
            .setPatch(patch)
            .build();
    }

    /**
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
