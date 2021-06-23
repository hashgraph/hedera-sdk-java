package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ScheduleCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.LinkedHashMap;
import java.util.Objects;
import javax.annotation.Nullable;

public final class ScheduleCreateTransaction extends Transaction<ScheduleCreateTransaction> {
    private final ScheduleCreateTransactionBody.Builder builder;

    AccountId payerAccountId;

    public ScheduleCreateTransaction() {
        builder = ScheduleCreateTransactionBody.newBuilder();

        setMaxTransactionFee(new Hbar(5));
    }

    ScheduleCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getScheduleCreate().toBuilder();

        if (builder.hasPayerAccountID()) {
            payerAccountId = AccountId.fromProtobuf(builder.getPayerAccountID());
        }
    }

    public AccountId getPayerAccountId() {
        return payerAccountId;
    }

    public ScheduleCreateTransaction setPayerAccountId(AccountId accountId) {
        requireNotFrozen();
        this.payerAccountId = accountId;
        return this;
    }

    public ScheduleCreateTransaction setScheduledTransaction(Transaction<?> transaction) {
        requireNotFrozen();

        var scheduled = transaction.schedule();
        builder.setScheduledTransactionBody(scheduled.builder.getScheduledTransactionBody());

        return this;
    }

    ScheduleCreateTransaction setScheduledTransactionBody(SchedulableTransactionBody tx) {
        requireNotFrozen();
        builder.setScheduledTransactionBody(tx);
        return this;
    }

    public Key getAdminKey() {
        return Key.fromProtobufKey(builder.getAdminKey());
    }

    public ScheduleCreateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        builder.setAdminKey(key.toProtobufKey());
        return this;
    }

    public String getScheduleMemo() {
        return builder.getMemo();
    }

    public ScheduleCreateTransaction setScheduleMemo(String memo) {
        requireNotFrozen();
        builder.setMemo(memo);
        return this;
    }

    ScheduleCreateTransactionBody.Builder build() {
        if (payerAccountId != null) {
            builder.setPayerAccountID(payerAccountId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateNetworkOnIds(@Nullable AccountId accountId) {
        EntityIdHelper.validateNetworkOnIds(this.payerAccountId, accountId);
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ScheduleServiceGrpc.getCreateScheduleMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleCreate(build());
        return true;
    }

    @Override
    final com.hedera.hashgraph.sdk.TransactionResponse mapResponse(
        com.hedera.hashgraph.sdk.proto.TransactionResponse transactionResponse,
        AccountId nodeId,
        com.hedera.hashgraph.sdk.proto.Transaction request
    ) {
        var transactionId = Objects.requireNonNull(getTransactionId()).setScheduled(true);
        var hash = hash(request.getSignedTransactionBytes().toByteArray());
        nextTransactionIndex = (nextTransactionIndex + 1) % transactionIds.size();
        return new com.hedera.hashgraph.sdk.TransactionResponse(nodeId, transactionId, hash, transactionId);
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        throw new IllegalStateException("Cannot schedule `ScheduleCreateTransaction`");
    }
}
