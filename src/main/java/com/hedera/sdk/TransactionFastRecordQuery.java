package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public final class TransactionFastRecordQuery extends QueryBuilder<TransactionGetFastRecordResponse> {
    private final com.hedera.sdk.proto.TransactionGetFastRecordQuery.Builder builder;

    public TransactionFastRecordQuery() {
        super(Response::getTransactionGetFastRecord);
        builder = inner.getTransactionGetFastRecordBuilder();
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
        require(builder.getTransactionIDOrBuilder(), ".setTransaction() required");
    }
}
