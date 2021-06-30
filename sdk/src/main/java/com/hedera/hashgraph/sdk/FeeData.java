package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import javax.annotation.Nullable;
import com.google.common.base.MoreObjects;

public class FeeData {
    @Nullable
    private FeeComponents nodeData;
    @Nullable
    private FeeComponents networkData;
    @Nullable
    private FeeComponents serviceData;

    public FeeData() {
        nodeData = null;
        networkData = null;
        serviceData = null;
    }

    static FeeData fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeData feeData) {
        return new FeeData()
            .setNodeData(feeData.hasNodedata() ? FeeComponents.fromProtobuf(feeData.getNodedata()) : null)
            .setNetworkData(feeData.hasNetworkdata() ? FeeComponents.fromProtobuf(feeData.getNetworkdata()) : null)
            .setServiceData(feeData.hasNodedata() ? FeeComponents.fromProtobuf(feeData.getServicedata()) : null);
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

    com.hedera.hashgraph.sdk.proto.FeeData toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.FeeData.newBuilder()
            .setNodedata(nodeData != null ? nodeData.toProtobuf() : null)
            .setNetworkdata(networkData != null ? networkData.toProtobuf() : null)
            .setServicedata(serviceData != null ? serviceData.toProtobuf() : null)
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("nodeData", getNodeData())
            .add("networkData", getNetworkData())
            .add("serviceData", getServiceData())
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}