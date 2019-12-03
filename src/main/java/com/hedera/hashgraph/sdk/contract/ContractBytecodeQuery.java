package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hederahashgraph.api.proto.java.ContractGetBytecodeQuery;
import com.hederahashgraph.api.proto.java.ContractGetBytecodeResponse;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.service.proto.java.SmartContractServiceGrpc;

import io.grpc.MethodDescriptor;

// `ContractGetBytecodeQuery`
public final class ContractBytecodeQuery extends QueryBuilder<ContractGetBytecodeResponse, ContractBytecodeQuery> {
    private final ContractGetBytecodeQuery.Builder builder = inner.getContractGetBytecodeBuilder();

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public ContractBytecodeQuery(Client client) {
        super(client);
    }

    public ContractBytecodeQuery() {
        super(null);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public ContractBytecodeQuery setContractId(ContractId contract) {
        builder.setContractID(contract.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContractId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getContractGetBytecodeMethod();
    }

    @Override
    protected ContractGetBytecodeResponse fromResponse(Response raw) {
        return raw.getContractGetBytecodeResponse();
    }
}
