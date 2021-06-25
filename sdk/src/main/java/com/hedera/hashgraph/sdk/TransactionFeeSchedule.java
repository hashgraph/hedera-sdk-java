package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import javax.annotation.Nullable;
import com.google.common.base.MoreObjects;

public class TransactionFeeSchedule {
    private RequestType requestType;
    @Nullable
    private FeeData feeData;

    public TransactionFeeSchedule() {
        requestType = RequestType.NONE;
        feeData = null;
    }

    static TransactionFeeSchedule fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule transactionFeeSchedule) {
        return new TransactionFeeSchedule()
            .setRequestType(RequestType.valueOf(transactionFeeSchedule.getHederaFunctionality()))
            .setFeeData(transactionFeeSchedule.hasFeeData() ? FeeData.fromProtobuf(transactionFeeSchedule.getFeeData()) : null);
    }

    public static TransactionFeeSchedule fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule.parseFrom(bytes).toBuilder().build());
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public TransactionFeeSchedule setRequestType(RequestType requestType) {
        this.requestType = requestType;
        return this;
    }

    @Nullable
    public FeeData getFeeData() {
        return feeData;
    }
    
    public TransactionFeeSchedule setFeeData(@Nullable FeeData feeData) {
        this.feeData = feeData;
        return this;
    }

    com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule toProtobuf() {
        var returnBuilder = com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule.newBuilder()
            .setHederaFunctionality(getRequestType().code);
        if(feeData != null) {
            returnBuilder.setFeeData(feeData.toProtobuf());
        }
        return returnBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("requestType", getRequestType())
            .add("feeData", getFeeData())
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}