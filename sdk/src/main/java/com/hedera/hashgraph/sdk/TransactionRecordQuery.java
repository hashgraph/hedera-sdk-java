package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.TransactionGetRecordQuery;
import io.grpc.MethodDescriptor;

public final class TransactionRecordQuery extends QueryBuilder<TransactionRecord, TransactionRecordQuery> {
    private final TransactionGetRecordQuery.Builder builder;

    public TransactionRecordQuery() {
        this.builder = TransactionGetRecordQuery.newBuilder();
    }

    public TransactionRecordQuery setTransactionId(TransactionId transactionId) {
        builder.setTransactionID(transactionId.toProtobuf());
        return this;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setTransactionGetRecord(builder.setHeader(header));
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getTransactionGetRecord().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getTransactionGetRecord().getHeader();
    }

    @Override
    protected TransactionRecord mapResponse(Response response) {
        return TransactionRecord.fromProtobuf(response.getTransactionGetRecord().getTransactionRecord());
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetTxRecordByTxIDMethod();
    }
}
