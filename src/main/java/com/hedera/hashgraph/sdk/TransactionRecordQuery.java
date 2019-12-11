package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.TransactionGetRecordQuery;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;

import io.grpc.MethodDescriptor;

public class TransactionRecordQuery extends QueryBuilder<TransactionRecord, TransactionRecordQuery> {
    private final TransactionGetRecordQuery.Builder builder = inner.getTransactionGetRecordBuilder();

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public TransactionRecordQuery(Client client) {
        super(client);
    }

    public TransactionRecordQuery() {
        super(null);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public TransactionRecordQuery setTransactionId(TransactionId transaction) {
        builder.setTransactionID(transaction.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetTxRecordByTxIDMethod();
    }

    @Override
    protected TransactionRecord fromResponse(Response raw) {
        return new TransactionRecord(
                raw.getTransactionGetRecord()
                    .getTransactionRecord());
    }

    @Override
    protected void doValidate() {
        require(builder.hasTransactionID(), ".setTransactionId() required");
    }
}
