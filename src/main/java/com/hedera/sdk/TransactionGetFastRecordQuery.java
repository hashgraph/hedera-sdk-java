package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class TransactionGetFastRecordQuery extends QueryBuilder<TransactionGetFastRecordResponse> {
    private final com.hedera.sdk.proto.TransactionGetFastRecordQuery.Builder builder;

    public TransactionGetFastRecordQuery() {
        super(Response::getTransactionGetFastRecord);
        builder = inner.getTransactionGetFastRecordBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public TransactionGetFastRecordQuery setTransaction(TransactionId transactionId) {
        builder.setTransactionID(transactionId.inner);
        return this;
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetFastTransactionRecordMethod();
    }
}
