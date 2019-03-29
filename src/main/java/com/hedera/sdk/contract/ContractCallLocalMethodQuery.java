package com.hedera.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.sdk.ContractId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `ContractCallLocalQuery`
public class ContractCallLocalMethodQuery extends QueryBuilder<ContractCallLocalResponse> {
    private final com.hedera.sdk.proto.ContractCallLocalQuery.Builder builder;

    public ContractCallLocalMethodQuery() {
        super(Response::getContractCallLocal);
        builder = inner.getContractCallLocalBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getContractCallLocalMethodMethod();
    }

    public ContractCallLocalMethodQuery setContract(ContractId id) {
        builder.setContractID(id.toProto());
        return this;
    }

    public ContractCallLocalMethodQuery setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    public ContractCallLocalMethodQuery setFunctionParameters(byte[] parameters) {
        builder.setFunctionParameters(ByteString.copyFrom(parameters));
        return this;
    }

    public ContractCallLocalMethodQuery setMaxResultSize(long size) {
        builder.setMaxResultSize(size);
        return this;
    }
}
