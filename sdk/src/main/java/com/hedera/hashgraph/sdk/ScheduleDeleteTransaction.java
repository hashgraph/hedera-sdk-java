package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ScheduleDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
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

    public ScheduleId getScheduleId() {
        return ScheduleId.fromProtobuf(builder.getScheduleID());
    }

    public ScheduleDeleteTransaction setScheduleId(ScheduleId id) {
        requireNotFrozen();
        builder.setScheduleID(id.toProtobuf());
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
}
