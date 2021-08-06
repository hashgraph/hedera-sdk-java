package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ScheduleSignTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import java.util.Objects;

public final class ScheduleSignTransaction extends Transaction<ScheduleSignTransaction> {

    @Nullable
    private ScheduleId scheduleId = null;

    public ScheduleSignTransaction() {
        setMaxTransactionFee(new Hbar(5));
    }

    ScheduleSignTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
    }

    @Nullable
    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    public ScheduleSignTransaction setScheduleId(ScheduleId scheduleId) {
        Objects.requireNonNull(scheduleId);
        requireNotFrozen();
        this.scheduleId = scheduleId;
        return this;
    }

    public ScheduleSignTransaction clearScheduleId() {
        requireNotFrozen();
        this.scheduleId = null;
        return this;
    }

    ScheduleSignTransactionBody.Builder build() {
        var builder = ScheduleSignTransactionBody.newBuilder();
        if (scheduleId != null) {
            builder.setScheduleID(scheduleId.toProtobuf());
        }

        return builder;
    }

    @Override
    void initFromTransactionBody(TransactionBody txBody) {
        var body = txBody.getScheduleSign();
        if (body.hasScheduleID()) {
            scheduleId = ScheduleId.fromProtobuf(body.getScheduleID());
        }
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (scheduleId != null) {
            scheduleId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ScheduleServiceGrpc.getSignScheduleMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleSign(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        throw new IllegalStateException("cannot schedule `ScheduleSignTransaction`");
    }
}
