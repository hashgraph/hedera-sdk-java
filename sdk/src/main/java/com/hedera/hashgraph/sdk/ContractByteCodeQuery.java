package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractGetBytecodeQuery;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Get the bytecode for a smart contract instance.
 */
public final class ContractByteCodeQuery extends Query<ByteString, ContractByteCodeQuery> {
    private final ContractGetBytecodeQuery.Builder builder;

    ContractId contractId;

    public ContractByteCodeQuery() {
        this.builder = ContractGetBytecodeQuery.newBuilder();
    }

    public ContractId getContractId() {
        if (contractId == null) {
            return new ContractId(0);
        }

        return contractId;
    }

    /**
     * Sets the contract ID for which information is requested.
     *
     * @return {@code this}
     * @param contractId The ContractId to be set
     */
    public ContractByteCodeQuery setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        this.contractId = contractId;
        return this;
    }

    @Override
    void validateNetworkOnIds(@Nullable AccountId accountId) {
        EntityIdHelper.validateNetworkOnIds(this.contractId, accountId);
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }

        queryBuilder.setContractGetBytecode(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getContractGetBytecodeResponse().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getContractGetBytecode().getHeader();
    }

    @Override
    ByteString mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return response.getContractGetBytecodeResponse().getBytecode();
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getContractGetBytecodeMethod();
    }
}
