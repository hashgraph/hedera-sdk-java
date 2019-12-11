package com.hedera.hashgraph.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.CallParams;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FunctionResult;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.proto.ContractCallLocalQuery;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;

import io.grpc.MethodDescriptor;

/** Call a function without updating its state or requiring concensus */
// `ContractCallLocalQuery`
public class ContractCallQuery extends QueryBuilder<FunctionResult, ContractCallQuery> {
    private final ContractCallLocalQuery.Builder builder = inner.getContractCallLocalBuilder();

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public ContractCallQuery(Client client) {
        super(client);
    }

    public ContractCallQuery() {
        super(null);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getContractCallLocalMethodMethod();
    }

    @Override
    protected FunctionResult fromResponse(Response raw) {
        if (!raw.hasContractCallLocal()) {
            throw new IllegalArgumentException("response was not `ContractCallLocal`");
        }

        return new FunctionResult(
                raw.getContractCallLocal()
                    .getFunctionResultOrBuilder());
    }

    public ContractCallQuery setContractId(ContractId id) {
        builder.setContractID(id.toProto());
        return this;
    }

    public ContractCallQuery setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    public ContractCallQuery setFunctionParameters(byte[] parameters) {
        builder.setFunctionParameters(ByteString.copyFrom(parameters));
        return this;
    }

    public ContractCallQuery setFunctionParameters(CallParams<CallParams.Function> parameters) {
        builder.setFunctionParameters(parameters.toProto());
        return this;
    }

    public ContractCallQuery setMaxResultSize(long size) {
        builder.setMaxResultSize(size);
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContractId() required");
    }
}
