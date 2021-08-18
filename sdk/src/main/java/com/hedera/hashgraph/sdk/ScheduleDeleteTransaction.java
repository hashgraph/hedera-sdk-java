package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

public final class ScheduleDeleteTransaction extends Transaction<ScheduleDeleteTransaction> {

    @Nullable
    private ScheduleId scheduleId = null;

    public ScheduleDeleteTransaction() {
        defaultMaxTransactionFee = new Hbar(5);
    }

    ScheduleDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    ScheduleDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    public ScheduleDeleteTransaction setScheduleId(ScheduleId scheduleId) {
        Objects.requireNonNull(scheduleId);
        requireNotFrozen();
        this.scheduleId = scheduleId;
        return this;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getScheduleDelete();
        if (body.hasScheduleID()) {
            scheduleId = ScheduleId.fromProtobuf(body.getScheduleID());
        }
    }

    ScheduleDeleteTransactionBody.Builder build() {
        var builder = ScheduleDeleteTransactionBody.newBuilder();
        if (scheduleId != null) {
            builder.setScheduleID(scheduleId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (scheduleId != null) {
            scheduleId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ScheduleServiceGrpc.getDeleteScheduleMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleDelete(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setScheduleDelete(build());
    }
}
