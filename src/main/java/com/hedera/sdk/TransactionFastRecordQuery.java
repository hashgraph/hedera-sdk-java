package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public final class TransactionFastRecordQuery extends QueryBuilder<TransactionRecord> {
    private final TransactionGetFastRecordQuery.Builder builder = inner.getTransactionGetFastRecordBuilder();

    public TransactionFastRecordQuery(Client client) {
        super(client, TransactionRecord::new);
    }

    TransactionFastRecordQuery() {
        super(null, TransactionRecord::new);
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
    protected void doValidate() {
        require(builder.hasTransactionID(), ".setTransaction() required");
    }
}
