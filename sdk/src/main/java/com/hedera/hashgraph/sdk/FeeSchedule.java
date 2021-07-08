package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import java.util.ArrayList;
import org.threeten.bp.Instant;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;
import java.util.Objects;

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

    public FeeSchedule setTransactionFeeSchedules(List<TransactionFeeSchedule> transactionFeeSchedules) {
        this.transactionFeeSchedules = Objects.requireNonNull(transactionFeeSchedules);
        return this;
    }

    public FeeSchedule addTransactionFeeSchedule(TransactionFeeSchedule transactionFeeSchedule) {
        transactionFeeSchedules.add(Objects.requireNonNull(transactionFeeSchedule));
        return this;
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
        var returnBuilder = com.hedera.hashgraph.sdk.proto.FeeSchedule.newBuilder();
        if(expirationTime != null) {
            returnBuilder.setExpiryTime(InstantConverter.toSecondsProtobuf(expirationTime));
        }
        for(TransactionFeeSchedule tFeeSchedule : getTransactionFeeSchedules()) {
            returnBuilder.addTransactionFeeSchedule(tFeeSchedule.toProtobuf());
        }
        return returnBuilder.build();
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