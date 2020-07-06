package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractGetBytecodeQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;

/**
 * Get the bytecode for a smart contract instance.
 */
public final class ContractByteCodeQuery extends QueryBuilder<ByteString, ContractByteCodeQuery> {
    private final ContractGetBytecodeQuery.Builder builder;

    public ContractByteCodeQuery() {
        this.builder = ContractGetBytecodeQuery.newBuilder();
    }

    /**
     * Sets the contract ID for which information is requested.
     *
     * @return {@code this}
     * @param contractId The ContractId to be set
     */
    public ContractByteCodeQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    @Override
    void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setContractGetBytecode(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getContractGetBytecodeResponse().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(Query request) {
        return request.getContractGetBytecode().getHeader();
    }

    @Override
    ByteString mapResponse(Response response) {
        return response.getContractGetBytecodeResponse().getBytecode();
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getContractGetBytecodeMethod();
    }
}
