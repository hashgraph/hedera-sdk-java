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

    public static SemanticVersion fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.SemanticVersion.parseFrom(bytes));
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
}
