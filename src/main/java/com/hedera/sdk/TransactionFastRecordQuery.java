package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

public final class TransactionFastRecordQuery extends QueryBuilder<TransactionRecord> {
    private final TransactionGetFastRecordQuery.Builder builder = inner.getTransactionGetFastRecordBuilder();

    public TransactionFastRecordQuery(Client client) {
        super(client);
    }

    TransactionFastRecordQuery() {
        super((Client) null);
    }

    public TransactionFastRecordQuery(@Nullable ChannelHolder channel) {
        super(channel);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public TransactionFastRecordQuery setTransaction(TransactionId transactionId) {
        builder.setTransactionID(transactionId.inner);
        return this;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetFastTransactionRecordMethod();
    }

    @Override
    protected TransactionRecord mapResponse(Response raw) throws HederaException {
        return new TransactionRecord(raw);
    }

    @Override
    protected void doValidate() {
        require(builder.hasTransactionID(), ".setTransactionId() required");
    }

    @Override
    protected boolean isPaymentRequired() {
        return false;
    }
}
