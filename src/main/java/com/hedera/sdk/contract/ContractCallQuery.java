package com.hedera.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.sdk.Client;
import com.hedera.sdk.ContractId;
import com.hedera.sdk.FunctionResult;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

import java.util.function.Function;

/** Call a function without updating its state or requiring concensus */
// `ContractCallLocalQuery`
public class ContractCallQuery extends QueryBuilder<FunctionResult> {
    private final ContractCallLocalQuery.Builder builder = inner.getContractCallLocalBuilder();

    private static final Function<Response, FunctionResult> mapResponse = response -> {
        if (!response.hasContractCallLocal()) {
            throw new IllegalArgumentException("response was not `ContractCallLocal`");
        }

        return new FunctionResult(
                response.getContractCallLocal()
                    .getFunctionResult()
        );
    };

    public ContractCallQuery(Client client) {
        super(client, mapResponse);
    }

    ContractCallQuery() {
        super(null, mapResponse);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getContractCallLocalMethodMethod();
    }

    public ContractCallQuery setContract(ContractId id) {
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

    public ContractCallQuery setMaxResultSize(long size) {
        builder.setMaxResultSize(size);
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContract() required");
    }
}
