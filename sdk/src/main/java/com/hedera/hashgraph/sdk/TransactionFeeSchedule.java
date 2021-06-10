package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import javax.annotation.Nullable;
import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.ArrayList;

public class TransactionFeeSchedule {
    private RequestType requestType;
    @Nullable
    private List<FeeData> fees = new ArrayList<>();

    public TransactionFeeSchedule() {
        requestType = RequestType.NONE;
    }

    static TransactionFeeSchedule fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule transactionFeeSchedule) {
        var returnTransactionFeeSchedule = new TransactionFeeSchedule()
            .setRequestType(RequestType.valueOf(transactionFeeSchedule.getHederaFunctionality()));
        if(transactionFeeSchedule.hasFeeData()) {
            returnTransactionFeeSchedule.getFees().add(FeeData.fromProtobuf(transactionFeeSchedule.getFeeData()));
        }
        return returnTransactionFeeSchedule;
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

    List<FeeData> getFees() {
        return fees;
    }

    com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule.newBuilder()
            .setHederaFunctionality(getRequestType().code)
            .setFeeData(fees.isEmpty() ? fees.get(0).toProtobuf() : null)
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("requestType", getRequestType())
            .add("fees", getFees())
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}