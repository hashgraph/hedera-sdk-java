package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public final class ContractGetInfoQuery extends QueryBuilder<ContractGetInfoResponse> {
    private final com.hedera.sdk.proto.ContractGetInfoQuery.Builder builder;

    public ContractGetInfoQuery() {
        super(Response::getContractGetInfo);
        builder = inner.getContractGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public ContractGetInfoQuery setContract(ContractId contract) {
        builder.setContractID(contract.inner);
        return this;
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getGetContractInfoMethod();
    }
}
