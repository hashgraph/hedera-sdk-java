package com.hedera.sdk.contract;

import com.hedera.sdk.ContractId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `ContractGetInfoQuery`
public final class ContractInfoQuery extends QueryBuilder<ContractGetInfoResponse> {
    private final com.hedera.sdk.proto.ContractGetInfoQuery.Builder builder;

    public ContractInfoQuery() {
        super(Response::getContractGetInfo);
        builder = inner.getContractGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public ContractInfoQuery setContract(ContractId contract) {
        builder.setContractID(contract.toProto());
        return this;
    }

    @Override
    public MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getGetContractInfoMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.getContractIDOrBuilder(), ".setContract() required");
    }
}
