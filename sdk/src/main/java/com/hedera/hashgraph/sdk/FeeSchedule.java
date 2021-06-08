package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import java.util.ArrayList;
import org.threeten.bp.Instant;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;

public class FeeSchedule {
    private List<TransactionFeeSchedule> transactionFeeSchedules = new ArrayList<>();
    @Nullable
    private Instant expirationTime;

    public FeeSchedule() {
    }

    static FeeSchedule fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeSchedule feeSchedule) {
        FeeSchedule returnFeeSchedule = new FeeSchedule()
            .setExpirationTime(feeSchedule.hasExpiryTime() ? InstantConverter.fromProtobuf(feeSchedule.getExpiryTime()) : null);
        for(var transactionFeeSchedule : feeSchedule.getTransactionFeeScheduleList()) {
            returnFeeSchedule
                .getTransactionFeeSchedules()
                .add(TransactionFeeSchedule.fromProtobuf(transactionFeeSchedule));
        }
        return returnFeeSchedule;
    }
    
    public static FeeSchedule fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeSchedule.parseFrom(bytes).toBuilder().build());
    }

    public List<TransactionFeeSchedule> getTransactionFeeSchedules() {
        return transactionFeeSchedules;
    }

    @Nullable
    public Instant getExpirationTime() {
        return expirationTime;
    }

    public FeeSchedule setExpirationTime(@Nullable Instant expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    com.hedera.hashgraph.sdk.proto.FeeSchedule toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.FeeSchedule.newBuilder()
            .setExpiryTime(expirationTime != null ? InstantConverter.toSecondsProtobuf(expirationTime) : null);
        for(TransactionFeeSchedule tFeeSchedule : getTransactionFeeSchedules()) {
            builder.addTransactionFeeSchedule(tFeeSchedule.toProtobuf());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("transactionFeeSchedules", getTransactionFeeSchedules())
            .add("expirationTime", getExpirationTime())
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}