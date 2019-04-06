package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class TransactionRecordQuery extends QueryBuilder<TransactionRecord> {
    private final com.hedera.sdk.proto.TransactionGetRecordQuery.Builder builder;

    public TransactionRecordQuery() {
        super(TransactionRecord::new);
        builder = inner.getTransactionGetRecordBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public TransactionRecordQuery setTransaction(TransactionId transaction) {
        builder.setTransactionID(transaction.inner);
        return this;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetTxRecordByTxIDMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasTransactionID(), ".setTransaction() required");
    }
}
