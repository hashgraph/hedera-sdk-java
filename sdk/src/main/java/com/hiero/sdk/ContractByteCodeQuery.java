// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import com.google.protobuf.ByteString;
import com.hiero.sdk.proto.ContractGetBytecodeQuery;
import com.hiero.sdk.proto.QueryHeader;
import com.hiero.sdk.proto.Response;
import com.hiero.sdk.proto.ResponseHeader;
import com.hiero.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Get the bytecode for a smart contract instance.
 */
public final class ContractByteCodeQuery extends Query<ByteString, ContractByteCodeQuery> {
    @Nullable
    private ContractId contractId = null;

    /**
     * Constructor.
     */
    public ContractByteCodeQuery() {}

    /**
     * Extract the contract id.
     *
     * @return                          the contract id
     */
    @Nullable
    public ContractId getContractId() {
        return contractId;
    }

    /**
     * Sets the contract ID for which information is requested.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public ContractByteCodeQuery setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        this.contractId = contractId;
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (contractId != null) {
            contractId.validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hiero.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = ContractGetBytecodeQuery.newBuilder();
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
    QueryHeader mapRequestHeader(com.hiero.sdk.proto.Query request) {
        return request.getContractGetBytecode().getHeader();
    }

    @Override
    ByteString mapResponse(Response response, AccountId nodeId, com.hiero.sdk.proto.Query request) {
        return response.getContractGetBytecodeResponse().getBytecode();
    }

    @Override
    MethodDescriptor<com.hiero.sdk.proto.Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getContractGetBytecodeMethod();
    }
}
