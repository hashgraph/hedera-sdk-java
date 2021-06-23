package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ScheduleSignTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

public final class ScheduleSignTransaction extends Transaction<ScheduleSignTransaction> {
    private final ScheduleSignTransactionBody.Builder builder;

    ScheduleId scheduleId;

    public ScheduleSignTransaction() {
        builder = ScheduleSignTransactionBody.newBuilder();

        setMaxTransactionFee(new Hbar(5));
    }

    ScheduleSignTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getScheduleSign().toBuilder();

        if (builder.hasScheduleID()) {
            scheduleId = ScheduleId.fromProtobuf(builder.getScheduleID());
        }
    }

    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    public ScheduleSignTransaction setScheduleId(ScheduleId scheduleId) {
        requireNotFrozen();
        this.scheduleId = scheduleId;
        return this;
    }

    public ScheduleSignTransaction clearScheduleId() {
        requireNotFrozen();
        builder.clearScheduleID();
        return this;
    }

    ScheduleSignTransactionBody.Builder build() {
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
        return ScheduleServiceGrpc.getSignScheduleMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleSign(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        throw new IllegalStateException("cannot schedule `ScheduleSignTransaction`");
    }
}
