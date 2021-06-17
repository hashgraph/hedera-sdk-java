package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

import java.util.Objects;

/**
 * Get information about a smart contract instance.
 * <p>
 * This includes the account that it uses, the file containing its bytecode,
 * and the time when it will expire.
 */
public final class ContractInfoQuery extends Query<ContractInfo, ContractInfoQuery> {
    private final ContractGetInfoQuery.Builder builder;

    public ContractInfoQuery() {
        builder = ContractGetInfoQuery.newBuilder();
    }

    public ContractId getContractId() {
      return ContractId.fromProtobuf(builder.getContractID());
    }

    /**
     * Sets the contract ID for which information is requested.
     *
     * @return {@code this}
     * @param contractId The ContractId to be set
     */
    public ContractInfoQuery setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        builder.setContractID(contractId.toProtobuf());

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
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
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
