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
