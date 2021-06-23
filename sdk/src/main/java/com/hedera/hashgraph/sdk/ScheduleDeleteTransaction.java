package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

public final class ScheduleDeleteTransaction extends Transaction<ScheduleDeleteTransaction> {
    private final ScheduleDeleteTransactionBody.Builder builder;

    ScheduleId scheduleId;

    public ScheduleDeleteTransaction() {
        builder = ScheduleDeleteTransactionBody.newBuilder();

        setMaxTransactionFee(new Hbar(5));
    }

    ScheduleDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getScheduleDelete().toBuilder();

        if (builder.hasScheduleID()) {
            scheduleId = ScheduleId.fromProtobuf(builder.getScheduleID());
        }
    }

    ScheduleDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getScheduleDelete().toBuilder();

        if (builder.hasScheduleID()) {
            scheduleId = ScheduleId.fromProtobuf(builder.getScheduleID());
        }
    }

    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    public ScheduleDeleteTransaction setScheduleId(ScheduleId scheduleId) {
        requireNotFrozen();
        this.scheduleId = scheduleId;
        return this;
    }

    ScheduleDeleteTransactionBody.Builder build() {
        if (scheduleId != null) {
            builder.setScheduleID(scheduleId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateNetworkOnIds(@Nullable AccountId accountId) {
        EntityIdHelper.validateNetworkOnIds(this.scheduleId, accountId);
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ScheduleServiceGrpc.getDeleteScheduleMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleDelete(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setScheduleDelete(build());
    }
}
