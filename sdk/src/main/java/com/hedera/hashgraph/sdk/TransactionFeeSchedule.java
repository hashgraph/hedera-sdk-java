package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransactionFeeSchedule {
    private RequestType requestType;
    @Nullable
    private FeeData feeData;
    private final List<FeeData> fees;

    public TransactionFeeSchedule() {
        requestType = RequestType.NONE;
        feeData = null;
        fees = new ArrayList<>();
    }

    static TransactionFeeSchedule fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule transactionFeeSchedule) {
        var returnFeeSchedule = new TransactionFeeSchedule()
            .setRequestType(RequestType.valueOf(transactionFeeSchedule.getHederaFunctionality()))
            .setFeeData(transactionFeeSchedule.hasFeeData() ? FeeData.fromProtobuf(transactionFeeSchedule.getFeeData()) : null);
        for (var feeData : transactionFeeSchedule.getFeesList()) {
            returnFeeSchedule.addFee(FeeData.fromProtobuf(feeData));
        }
        return returnFeeSchedule;
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

    @Deprecated
    @Nullable
    public FeeData getFeeData() {
        return feeData;
    }

    @Deprecated
    public TransactionFeeSchedule setFeeData(@Nullable FeeData feeData) {
        this.feeData = feeData;
        return this;
    }

    public List<FeeData> getFees() {
        return fees;
    }

    public TransactionFeeSchedule addFee(FeeData fee) {
        fees.add(Objects.requireNonNull(fee));
        return this;
    }

    com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule toProtobuf() {
        var returnBuilder = com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule.newBuilder()
            .setHederaFunctionality(getRequestType().code);
        if (feeData != null) {
            returnBuilder.setFeeData(feeData.toProtobuf());
        }
        for (var fee : fees) {
            returnBuilder.addFees(fee.toProtobuf());
        }
        return returnBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("requestType", getRequestType())
            .add("feeData", getFeeData())
            .add("fees", getFees())
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
