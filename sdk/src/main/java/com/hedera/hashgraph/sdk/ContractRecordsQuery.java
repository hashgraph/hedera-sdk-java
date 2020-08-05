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

/**
 * Get all the records for a smart contract instance, for any function
 * call (or the constructor call) during the last 25 hours, for which a Record was requested.
 */
public final class ContractRecordsQuery extends QueryBuilder<List<TransactionRecord>, ContractRecordsQuery> {
    private final ContractGetRecordsQuery.Builder builder;

    public ContractRecordsQuery() {
        this.builder = ContractGetRecordsQuery.newBuilder();
    }

    /**
     * Sets the smart contract instance for which the records should be retrieved.
     *
     * @return {@code this}
     * @param contractId The ContractId to be set
     */
    public ContractRecordsQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    @Override
    void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setContractGetRecords(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getContractGetRecordsResponse().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(Query request) {
        return request.getContractGetRecords().getHeader();
    }

    @Override
    List<TransactionRecord> mapResponse(Response response, AccountId nodeId, Query request) {
        var rawTransactionRecords = response.getContractGetRecordsResponse().getRecordsList();
        var transactionRecords = new ArrayList<TransactionRecord>(rawTransactionRecords.size());

        for (var record : rawTransactionRecords) {
            transactionRecords.add(TransactionRecord.fromProtobuf(record));
        }

        return transactionRecords;
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getGetTxRecordByContractIDMethod();
    }
}
