package com.hedera.sdk.contract;

import com.hedera.sdk.Client;
import com.hedera.sdk.ContractId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `ContractGetInfoQuery`
public final class ContractInfoQuery extends QueryBuilder<ContractInfo> {
    private final ContractGetInfoQuery.Builder builder = inner.getContractGetInfoBuilder();

    public ContractInfoQuery(Client client) {
        super(client, ContractInfo::new);
    }

    ContractInfoQuery() {
        super(null, ContractInfo::new);
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
        require(builder.hasContractID(), ".setContract() required");
    }
}
