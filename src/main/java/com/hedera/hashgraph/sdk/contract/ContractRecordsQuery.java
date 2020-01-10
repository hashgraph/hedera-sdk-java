package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.proto.ContractGetRecordsQuery;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.TransactionRecord;

import java.util.List;
import java.util.stream.Collectors;

import io.grpc.MethodDescriptor;

/**
 * Get a list of {@link com.hedera.hashgraph.sdk.TransactionRecord}s involved with a contract.
 */
// `ContractGetRecordsQuery`
public class ContractRecordsQuery extends QueryBuilder<List<TransactionRecord>, ContractRecordsQuery> {
    private final ContractGetRecordsQuery.Builder builder = inner.getContractGetRecordsBuilder();

    public ContractRecordsQuery() {
        super();
    }

    public ContractRecordsQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getGetTxRecordByContractIDMethod();
    }

    @Override
    protected List<TransactionRecord> extractResponse(Response raw) {
        return raw.getContractGetRecordsResponse()
            .getRecordsList()
            .stream()
            .map(TransactionRecord::new)
            .collect(Collectors.toList());
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContractId() required");
    }
}
