package com.hedera.sdk.contract;

import com.hedera.sdk.Client;
import com.hedera.sdk.ContractId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `ContractGetBytecodeQuery`
public final class ContractBytecodeQuery extends QueryBuilder<ContractGetBytecodeResponse> {
    private final ContractGetBytecodeQuery.Builder builder = inner.getContractGetBytecodeBuilder();

    public ContractBytecodeQuery(Client client) {
        super(client, Response::getContractGetBytecodeResponse);
    }

    ContractBytecodeQuery() {
        super(null, Response::getContractGetBytecodeResponse);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public ContractBytecodeQuery setContract(ContractId contract) {
        builder.setContractID(contract.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContract() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getContractGetBytecodeMethod();
    }
}
