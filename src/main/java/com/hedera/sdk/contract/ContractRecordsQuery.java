package com.hedera.sdk.contract;

import com.hedera.sdk.ContractId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `ContractGetRecordsQuery`
public class ContractRecordsQuery extends QueryBuilder<ContractGetRecordsResponse> {
    private final com.hedera.sdk.proto.ContractGetRecordsQuery.Builder builder;

    public ContractRecordsQuery() {
        super(Response::getContractGetRecordsResponse);
        builder = inner.getContractGetRecordsBuilder();
    }

    public ContractRecordsQuery setContract(ContractId contractId) {
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
}
