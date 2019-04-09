package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public final class TransactionReceiptQuery extends QueryBuilder<TransactionReceipt> {
    private final TransactionGetReceiptQuery.Builder builder = inner.getTransactionGetReceiptBuilder();

    public TransactionReceiptQuery(Client client) {
        super(client, TransactionReceipt::new);
    }

    TransactionReceiptQuery() {
        super(null, TransactionReceipt::new);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return inner.getTransactionGetReceiptBuilder()
            .getHeaderBuilder();
    }

    public TransactionReceiptQuery setTransaction(TransactionId transactionId) {
        builder.setTransactionID(transactionId.inner);
        return this;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetTransactionReceiptsMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasTransactionID(), ".setTransaction() required");
    }
}
