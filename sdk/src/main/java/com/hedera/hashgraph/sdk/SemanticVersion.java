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

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Hedera follows semantic versioning () for both the HAPI protobufs and
 * the Services software. This type allows the getVersionInfo query in the
 * NetworkService to return the deployed versions of both protobufs and
 * software on the node answering the query.
 *
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/basic-types/semanticversion">Hedera Documentation</a>
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
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static SemanticVersion fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.SemanticVersion.parseFrom(bytes));
    }

    /**
     * Create the protobuf.
     *
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
     * Create the byte array.
     *
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
