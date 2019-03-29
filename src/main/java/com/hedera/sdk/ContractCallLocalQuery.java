package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class ContractCallLocalQuery extends QueryBuilder<ContractCallLocalResponse> {
    private final com.hedera.sdk.proto.ContractCallLocalQuery.Builder builder;

    ContractCallLocalQuery() {
        super(Response::getContractCallLocal);
        builder = inner.getContractCallLocalBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getContractCallLocalMethodMethod();
    }

    public ContractCallLocalQuery setContract(ContractId id) {
        builder.setContractID(id.inner);
        return this;
    }

    public ContractCallLocalQuery setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    public ContractCallLocalQuery setFunctionParameters(byte[] parameters) {
        builder.setFunctionParameters(ByteString.copyFrom(parameters));
        return this;
    }

    public ContractCallLocalQuery setMaxResultSize(long size) {
        builder.setMaxResultSize(size);
        return this;
    }
}
