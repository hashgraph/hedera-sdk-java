package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import java.util.ArrayList;

import org.threeten.bp.Instant;

public class FeeSchedule {
    private List<TransactionFeeSchedule> transactionFeeSchedules;
    private Instant expiryTime;

    public FeeSchedule() {
        transactionFeeSchedules = new ArrayList<TransactionFeeSchedule>();
    }

    List<TransactionFeeSchedule> getTransactionFeeSchedules() {
        return transactionFeeSchedules;
    }
    Instant getExpiryTime() {
        return expiryTime;
    }

    FeeSchedule setExpiryTime(Instant expiryTime) {
        this.expiryTime = expiryTime;
        return this;
    }


    static FeeSchedule fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeSchedule feeSchedule) {
        FeeSchedule returnFeeSchedule = new FeeSchedule();
        if(feeSchedule.hasExpiryTime()) {
            returnFeeSchedule.setExpiryTime(InstantConverter.fromProtobuf(feeSchedule.getExpiryTime()));
        }
        for(int i = 0; i < feeSchedule.getTransactionFeeScheduleCount(); i++) {
            returnFeeSchedule
                .getTransactionFeeSchedules()
                .add(TransactionFeeSchedule.fromProtobuf(feeSchedule.getTransactionFeeSchedule(i)));
        }
        return returnFeeSchedule;
    }
    public static FeeSchedule fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeSchedule.parseFrom(bytes).toBuilder().build());
    }



    com.hedera.hashgraph.sdk.proto.FeeSchedule toProtobuf()
    {
        var builder = com.hedera.hashgraph.sdk.proto.FeeSchedule.newBuilder();
        builder.setExpiryTime(InstantConverter.toSecondsProtobuf(getExpiryTime()));
        for(TransactionFeeSchedule tFeeSchedule : getTransactionFeeSchedules()) {
            builder.addTransactionFeeSchedule(tFeeSchedule.toProtobuf());
        }
        return builder.build();
    }
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}