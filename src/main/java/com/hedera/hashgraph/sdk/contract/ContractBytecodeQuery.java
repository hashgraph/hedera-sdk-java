package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.proto.ContractGetBytecodeQuery;
import com.hedera.hashgraph.proto.ContractGetBytecodeResponse;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;

import io.grpc.MethodDescriptor;

/**
 * Get the bytecode of a contract.
 *
 * @deprecated the result type of {@link ContractGetBytecodeResponse} returned from the various
 * {@code execute[Async](...)} methods is changing in 1.0 to {@code byte[]}, which
 * is a breaking change. This class is not being removed.
 */
@Deprecated
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
