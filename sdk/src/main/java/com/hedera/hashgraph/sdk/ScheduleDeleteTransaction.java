package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.LinkedHashMap;

public final class ScheduleDeleteTransaction extends Transaction<ScheduleDeleteTransaction> {
    private final ScheduleDeleteTransactionBody.Builder builder;

    public ScheduleDeleteTransaction() {
        builder = ScheduleDeleteTransactionBody.newBuilder();

        setMaxTransactionFee(new Hbar(5));
    }

    ScheduleDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getScheduleDelete().toBuilder();
    }

    ScheduleDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getScheduleDelete().toBuilder();
    }

    public ScheduleId getScheduleId() {
        return ScheduleId.fromProtobuf(builder.getScheduleID());
    }

    public ScheduleDeleteTransaction setScheduleId(ScheduleId scheduleId) {
        requireNotFrozen();
        builder.setScheduleID(scheduleId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ScheduleServiceGrpc.getDeleteScheduleMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleDelete(builder);
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setScheduleDelete(builder);
    }
}
