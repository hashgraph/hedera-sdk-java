/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Get information about a smart contract instance.
 * <p>
 * This includes the account that it uses, the file containing its bytecode,
 * and the time when it will expire.
 */
public final class ContractInfoQuery extends Query<ContractInfo, ContractInfoQuery> {
    @Nullable
    private ContractId contractId = null;

    /**
     * Constructor.
     */
    public ContractInfoQuery() {
    }

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
    public ContractInfoQuery setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        this.contractId = contractId;
        return this;
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `CONTRACT_DELETED` back instead.
        return super.getCostAsync(client).thenApply((cost) -> Hbar.fromTinybars(Math.max(cost.toTinybars(), 25)));
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (contractId != null) {
            contractId.validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = ContractGetInfoQuery.newBuilder();
        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }

        queryBuilder.setContractGetInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getContractGetInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getContractGetInfo().getHeader();
    }

    @Override
    ContractInfo mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return ContractInfo.fromProtobuf(response.getContractGetInfo().getContractInfo());
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getGetContractInfoMethod();
    }
}
