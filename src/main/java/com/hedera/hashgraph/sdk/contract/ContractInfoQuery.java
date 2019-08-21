package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hederahashgraph.api.proto.java.ContractGetInfoQuery;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.service.proto.java.SmartContractServiceGrpc;

import io.grpc.MethodDescriptor;

// `ContractGetInfoQuery`
public final class ContractInfoQuery extends QueryBuilder<ContractInfo, ContractInfoQuery> {
    private final ContractGetInfoQuery.Builder builder = inner.getContractGetInfoBuilder();

    public ContractInfoQuery(Client client) {
        super(client);
    }

    ContractInfoQuery() {
        super(null);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public ContractInfoQuery setContractId(ContractId contract) {
        builder.setContractID(contract.toProto());
        return this;
    }

    @Override
    public MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getGetContractInfoMethod();
    }

    @Override
    protected ContractInfo fromResponse(Response raw) {
        return new ContractInfo(raw);
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContractId() required");
    }
}
