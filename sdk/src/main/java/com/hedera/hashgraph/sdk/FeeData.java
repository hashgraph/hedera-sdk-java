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

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nullable;

public class FeeData {
    @Nullable
    private FeeComponents nodeData = null;
    @Nullable
    private FeeComponents networkData = null;
    @Nullable
    private FeeComponents serviceData = null;
    private FeeDataType type = FeeDataType.DEFAULT;

    public FeeData() {
    }

    static FeeData fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeData feeData) {
        return new FeeData()
            .setNodeData(feeData.hasNodedata() ? FeeComponents.fromProtobuf(feeData.getNodedata()) : null)
            .setNetworkData(feeData.hasNetworkdata() ? FeeComponents.fromProtobuf(feeData.getNetworkdata()) : null)
            .setServiceData(feeData.hasNodedata() ? FeeComponents.fromProtobuf(feeData.getServicedata()) : null)
            .setType(FeeDataType.valueOf(feeData.getSubType()));
    }

    public static FeeData fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeData.parseFrom(bytes).toBuilder().build());
    }

    @Nullable
    FeeComponents getNodeData() {
        return nodeData;
    }

    FeeData setNodeData(@Nullable FeeComponents nodeData) {
        this.nodeData = nodeData;
        return this;
    }

    @Nullable
    FeeComponents getNetworkData() {
        return networkData;
    }

    FeeData setNetworkData(@Nullable FeeComponents networkData) {
        this.networkData = networkData;
        return this;
    }

    @Nullable
    FeeComponents getServiceData() {
        return serviceData;
    }

    FeeData setServiceData(@Nullable FeeComponents serviceData) {
        this.serviceData = serviceData;
        return this;
    }

    FeeDataType getType() {
        return type;
    }

    FeeData setType(FeeDataType type) {
        this.type = type;
        return this;
    }

    com.hedera.hashgraph.sdk.proto.FeeData toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.FeeData.newBuilder().setSubType(type.code);
        if (nodeData != null) {
            builder.setNodedata(nodeData.toProtobuf());
        }
        if (networkData != null) {
            builder.setNetworkdata(networkData.toProtobuf());
        }
        if (serviceData != null) {
            builder.setServicedata(serviceData.toProtobuf());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("nodeData", getNodeData())
            .add("networkData", getNetworkData())
            .add("serviceData", getServiceData())
            .add("type", getType())
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
