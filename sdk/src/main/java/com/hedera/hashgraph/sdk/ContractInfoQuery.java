package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

/**
 * Get information about a smart contract instance.
 * <p>
 * This includes the account that it uses, the file containing its bytecode,
 * and the time when it will expire.
 */
public final class ContractInfoQuery extends QueryBuilder<ContractInfo, ContractInfoQuery> {
    private final ContractGetInfoQuery.Builder builder;

    public ContractInfoQuery() {
        builder = ContractGetInfoQuery.newBuilder();
    }

    /**
     * Sets the contract ID for which information is requested.
     *
     * @return {@code this}
     * @param contractId The ContractId to be set
     */
    public ContractInfoQuery setContractId(ContractId contractId) {
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
    void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setContractGetInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getContractGetInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(Query request) {
        return request.getContractGetInfo().getHeader();
    }

    @Override
    ContractInfo mapResponse(Response response, AccountId nodeId, Query request) {
        return ContractInfo.fromProtobuf(response.getContractGetInfo().getContractInfo());
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getGetContractInfoMethod();
    }
}
