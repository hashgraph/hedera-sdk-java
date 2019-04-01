package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public final class TransactionGetFastRecordQuery extends QueryBuilder<TransactionGetFastRecordResponse> {
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
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetFastTransactionRecordMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.getTransactionIDOrBuilder(), ".setTransaction() required");
    }
}
