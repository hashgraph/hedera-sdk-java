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

/**
 * Internal utility class.
 */
public class NetworkVersionInfo {
    /**
     * Version of the protobuf schema in use by the network
     */
    public final SemanticVersion protobufVersion;

    /**
     * Version of the Hedera services in use by the network
     */
    public final SemanticVersion servicesVersion;

    /**
     * Constructor.
     *
     * @param hapi                      the protobuf version
     * @param hedera                    the hedera version
     */
    NetworkVersionInfo(SemanticVersion hapi, SemanticVersion hedera) {
        this.protobufVersion = hapi;
        this.servicesVersion = hedera;
    }

    /**
     * Create a network version info object from a protobuf.
     *
     * @param proto                     the protobuf
     * @return                          the new network version object
     */
    protected static NetworkVersionInfo fromProtobuf(NetworkGetVersionInfoResponse proto) {
        return new NetworkVersionInfo(
            SemanticVersion.fromProtobuf(proto.getHapiProtoVersion()),
            SemanticVersion.fromProtobuf(proto.getHederaServicesVersion())
        );
    }

    /**
     * Create a network version info object from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new network version object
     * @throws InvalidProtocolBufferException
     */
    public static NetworkVersionInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(NetworkGetVersionInfoResponse.parseFrom(bytes));
    }

    /**
     * @return                          the protobuf representation
     */
    protected NetworkGetVersionInfoResponse toProtobuf() {
        return NetworkGetVersionInfoResponse.newBuilder()
            .setHapiProtoVersion(protobufVersion.toProtobuf())
            .setHederaServicesVersion(servicesVersion.toProtobuf())
            .build();
    }

    /**
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
