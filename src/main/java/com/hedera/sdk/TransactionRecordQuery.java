package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class TransactionRecordQuery extends QueryBuilder<TransactionRecord> {
    private final TransactionGetRecordQuery.Builder builder = inner.getTransactionGetRecordBuilder();

    public TransactionRecordQuery(Client client) {
        super(client);
    }

    TransactionRecordQuery() {
        super((Client) null);
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
    protected TransactionRecord mapResponse(Response raw) throws HederaException {
        return new TransactionRecord(
                raw.getTransactionGetRecord()
                    .getTransactionRecord()
        );
    }

    @Override
    protected void doValidate() {
        require(builder.hasTransactionID(), ".setTransactionId() required");
    }
}
