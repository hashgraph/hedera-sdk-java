package com.hedera.sdk;

import com.hedera.sdk.proto.Query;
import com.hedera.sdk.proto.QueryHeader;
import com.hedera.sdk.proto.Response;
import com.hedera.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;

public final class ContractGetBytecodeQuery
        extends QueryBuilder<com.hedera.sdk.proto.ContractGetBytecodeResponse> {
    private final com.hedera.sdk.proto.ContractGetBytecodeQuery.Builder builder;

    public ContractGetBytecodeQuery() {
        super(Response::getContractGetBytecodeResponse);
        builder = inner.getContractGetBytecodeBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public ContractGetBytecodeQuery setContract(ContractId contract) {
        builder.setContractID(contract.inner);
        return this;
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getContractGetBytecodeMethod();
    }
}
