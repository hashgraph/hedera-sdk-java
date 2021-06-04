package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import javax.annotation.Nullable;

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





    // getters

    boolean hasNodeData() {
        return nodeData != null;
    }
    boolean hasNetworkData() {
        return networkData != null;
    }
    boolean hasServiceData() {
        return serviceData != null;
    }
    FeeComponents getNodeData() {
        assert hasNodeData();
        return nodeData;
    }
    FeeComponents getNetworkData() {
        assert hasNetworkData();
        return networkData;
    }
    FeeComponents getServiceData() {
        assert hasServiceData();
        return serviceData;
    }



    // setters

    FeeData setNodeData(@Nullable FeeComponents nodeData) {
        this.nodeData = nodeData;
        return this;
    }
    FeeData setNetworkData(@Nullable FeeComponents networkData) {
        this.networkData = networkData;
        return this;
    }
    FeeData setServiceData(@Nullable FeeComponents serviceData) {
        this.serviceData = serviceData;
        return this;
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



    com.hedera.hashgraph.sdk.proto.FeeData toProtobuf()
    {
        var builder = com.hedera.hashgraph.sdk.proto.FeeData.newBuilder();
        if(hasNodeData()) {
            builder.setNodedata(getNodeData().toProtobuf());
        }
        if(hasNetworkData()) {
            builder.setNetworkdata(getNetworkData().toProtobuf());
        }
        if(hasServiceData()) {
            builder.setServicedata(getServiceData().toProtobuf());
        }
        return builder.build();
    }
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}