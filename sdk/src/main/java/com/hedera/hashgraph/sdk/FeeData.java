package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nullable;

/**
 * The total fees charged for a transaction. It contains three parts namely
 * node data, network data and service data.
 */
public class FeeData {
    @Nullable
    private FeeComponents nodeData = null;
    @Nullable
    private FeeComponents networkData = null;
    @Nullable
    private FeeComponents serviceData = null;
    private FeeDataType type = FeeDataType.DEFAULT;

    /**
     * Constructor.
     */
    public FeeData() {
    }

    /**
     * Initialize fee data object from a protobuf.
     *
     * @param feeData                   the protobuf
     * @return                          the fee data object
     */
    static FeeData fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeData feeData) {
        return new FeeData()
            .setNodeData(feeData.hasNodedata() ? FeeComponents.fromProtobuf(feeData.getNodedata()) : null)
            .setNetworkData(feeData.hasNetworkdata() ? FeeComponents.fromProtobuf(feeData.getNetworkdata()) : null)
            .setServiceData(feeData.hasNodedata() ? FeeComponents.fromProtobuf(feeData.getServicedata()) : null)
            .setType(FeeDataType.valueOf(feeData.getSubType()));
    }

    /**
     * Initialize fee data object from byte array.
     *
     * @param bytes                     the byte array
     * @return                          the fee data object
     * @throws InvalidProtocolBufferException
     */
    public static FeeData fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeData.parseFrom(bytes).toBuilder().build());
    }

    /**
     * @return                          the node data fee components object
     */
    @Nullable
    FeeComponents getNodeData() {
        return nodeData;
    }

    /**
     * Assign the node data fee component object.
     *
     * @param nodeData                  the node data fee component object
     * @return {@code this}
     */
    FeeData setNodeData(@Nullable FeeComponents nodeData) {
        this.nodeData = nodeData;
        return this;
    }

    /**
     * @return                          the network data fee component object
     */
    @Nullable
    FeeComponents getNetworkData() {
        return networkData;
    }

    /**
     * Assign the network data fee component object.
     *
     * @param networkData               the network data fee component object
     * @return {@code this}
     */
    FeeData setNetworkData(@Nullable FeeComponents networkData) {
        this.networkData = networkData;
        return this;
    }

    /**
     * @return                          the service data fee component object
     */
    @Nullable
    FeeComponents getServiceData() {
        return serviceData;
    }

    /**
     * Assign the service data fee component object.
     *
     * @param serviceData               the service data fee component object
     * @return {@code this}
     */
    FeeData setServiceData(@Nullable FeeComponents serviceData) {
        this.serviceData = serviceData;
        return this;
    }

    /**
     * @return                          the fee data type
     */
    FeeDataType getType() {
        return type;
    }

    /**
     * Assign the fee data type.
     * {@link com.hedera.hashgraph.sdk.FeeDataType}
     *
     * @param type                      the fee data type
     * @return {@code this}
     */
    FeeData setType(FeeDataType type) {
        this.type = type;
        return this;
    }

    /**
     * Convert the fee data type into a protobuf.
     *
     * @return                          the protobuf
     */
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

    /**
     * @return                          a byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
