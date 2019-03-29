package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class ContractGetRecordsQuery extends QueryBuilder<ContractGetRecordsResponse> {
    private final com.hedera.sdk.proto.ContractGetRecordsQuery.Builder builder;

    public ContractGetRecordsQuery() {
        super(Response::getContractGetRecordsResponse);
        builder = inner.getContractGetRecordsBuilder();
    }

    public ContractGetRecordsQuery setContract(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getGetTxRecordByContractIDMethod();
    }
}
