package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractCallLocalQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

public final class ContractCallQuery extends QueryBuilder<ContractFunctionResult, ContractCallQuery> {
    private final ContractCallLocalQuery.Builder builder;

    public ContractCallQuery() {
        builder = ContractCallLocalQuery.newBuilder();
    }

    public ContractCallQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    public ContractCallQuery setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // network bug: ContractCallLocal cost estimate is too low
        return super.getCostAsync(client).thenApply(cost -> Hbar.fromTinybar((long) (cost.asTinybar() * 1.1)));
    }

    public ContractCallQuery setFunctionParameters(ByteString functionParameters) {
        builder.setFunctionParameters(functionParameters);
        return this;
    }

    public ContractCallQuery setFunction(String name) {
        return setFunction(name, new ContractFunctionParameters());
    }

    public ContractCallQuery setFunction(String name, ContractFunctionParameters params) {
        return setFunctionParameters(params.toBytes(name));
    }

    public ContractCallQuery setMaxResultSize(long size) {
        builder.setMaxResultSize(size);
        return this;
    }

    @Override
    void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setContractCallLocal(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getContractCallLocal().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(Query request) {
        return request.getContractCallLocal().getHeader();
    }

    @Override
    ContractFunctionResult mapResponse(Response response) {
        return new ContractFunctionResult(response.getContractCallLocal().getFunctionResult());
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getContractCallLocalMethodMethod();
    }
}
