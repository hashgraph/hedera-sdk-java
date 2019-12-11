package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.proto.ContractGetInfoQuery;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;

import io.grpc.MethodDescriptor;

// `ContractGetInfoQuery`
public final class ContractInfoQuery extends QueryBuilder<ContractInfo, ContractInfoQuery> {
    private final ContractGetInfoQuery.Builder builder = inner.getContractGetInfoBuilder();

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public ContractInfoQuery(Client client) {
        super(client);
    }

    public ContractInfoQuery() {
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
        return ContractInfo.fromResponse(raw);
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContractId() required");
    }
}
