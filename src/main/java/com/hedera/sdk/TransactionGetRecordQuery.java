package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class TransactionGetRecordQuery extends QueryBuilder<TransactionGetRecordResponse> {
    private final com.hedera.sdk.proto.TransactionGetRecordQuery.Builder builder;

    public TransactionGetRecordQuery() {
        super(Response::getTransactionGetRecord);
        builder = inner.getTransactionGetRecordBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public TransactionGetRecordQuery setTransaction(TransactionId transaction) {
        builder.setTransactionID(transaction.inner);
        return this;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        // FIXME does not have a corresponding service method
        throw new Error("not implemented");
    }

    @Override
    protected void doValidate() {
        require(builder.getTransactionIDOrBuilder(), ".setTransaction() required");
    }
}
