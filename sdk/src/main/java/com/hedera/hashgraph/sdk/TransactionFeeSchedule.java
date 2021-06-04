package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import javax.annotation.Nullable;

public class TransactionFeeSchedule {
    private RequestType requestType;
    @Nullable
    private FeeData feeData;

    public TransactionFeeSchedule() {
        requestType = RequestType.NONE;
        feeData = null;
    }

    RequestType getRequestType() {
        return requestType;
    }
    boolean hasFeeData() {
        return feeData != null;
    }
    @Nullable
    FeeData getFeeData() {
        assert hasFeeData();
        return feeData;
    }

    TransactionFeeSchedule setRequestType(RequestType requestType) {
        this.requestType = requestType;
        return this;
    }
    TransactionFeeSchedule setFeeData(@Nullable FeeData feeData)
    {
        this.feeData = feeData;
        return this;
    }

    static TransactionFeeSchedule fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule transactionFeeSchedule) {
        return new TransactionFeeSchedule()
            .setRequestType(RequestType.valueOf(transactionFeeSchedule.getHederaFunctionality()))
            .setFeeData(transactionFeeSchedule.hasFeeData() ? FeeData.fromProtobuf(transactionFeeSchedule.getFeeData()) : null);
    }
    public static TransactionFeeSchedule fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule.parseFrom(bytes).toBuilder().build());
    }



    com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule toProtobuf()
    {
        var builder = com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule.newBuilder();
        builder.setHederaFunctionality(getRequestType().code);
        if(hasFeeData()) {
            builder.setFeeData(getFeeData().toProtobuf());
        }
        return builder.build();
    }
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}