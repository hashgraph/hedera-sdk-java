package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractGetRecordsQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.List;

public final class ContractRecordsQuery extends QueryBuilder<List<TransactionRecord>, ContractRecordsQuery> {
    private final ContractGetRecordsQuery.Builder builder;

    public ContractRecordsQuery() {
        this.builder = ContractGetRecordsQuery.newBuilder();
    }

    public ContractRecordsQuery setContractId(ContractId accountId) {
        builder.setContractID(accountId.toProtobuf());
        return this;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setContractGetRecords(builder.setHeader(header));
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getContractGetRecordsResponse().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getContractGetRecords().getHeader();
    }

    @Override
    protected List<TransactionRecord> mapResponse(Response response) {
        var rawTransactionRecords = response.getContractGetRecordsResponse().getRecordsList();
        var transactionRecords = new ArrayList<TransactionRecord>(rawTransactionRecords.size());

        for (var record : rawTransactionRecords) {
            transactionRecords.add(TransactionRecord.fromProtobuf(record));
        }

        return transactionRecords;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getGetTxRecordByContractIDMethod();
    }
}
