package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ScheduleCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.LinkedHashMap;
import java.util.Objects;

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
    void validateNetworkOnIds(Client client) {
        if (payerAccountId != null) {
            payerAccountId.validate(client);
        }
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
        TransactionResponse transactionResponse,
        AccountId nodeId,
        com.hedera.hashgraph.sdk.proto.Transaction request,
        @Nullable NetworkName networkName) {
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
