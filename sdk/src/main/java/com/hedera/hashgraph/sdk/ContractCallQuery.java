package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractCallLocalQuery;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Call a function of the given smart contract instance, giving it functionParameters as its inputs.
 * It will consume the entire given amount of gas.
 * <p>
 * This is performed locally on the particular node that the client is communicating with.
 * It cannot change the state of the contract instance (and so, cannot spend
 * anything from the instance's cryptocurrency account).
 * <p>
 * It will not have a consensus timestamp. It cannot generate a record or a receipt.
 * The response will contain the output returned by the function call.
 * This is useful for calling getter functions, which purely read the state and don't change it.
 * It is faster and cheaper than a normal call, because it is purely local to a single  node.
 */
public final class ContractCallQuery extends Query<ContractFunctionResult, ContractCallQuery> {
    private final ContractCallLocalQuery.Builder builder;

    ContractId contractId;

    public ContractCallQuery() {
        builder = ContractCallLocalQuery.newBuilder();
    }

    public ContractId getContractId() {
        if (contractId == null) {
            return new ContractId(0);
        }

        return contractId;
    }

    /**
     * Sets the contract instance to call, in the format used in transactions.
     *
     * @return {@code this}
     * @param contractId The ContractId to be set
     */
    public ContractCallQuery setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        this.contractId = contractId;
        return this;
    }

    public long getGas() {
      return builder.getGas();
    }

    /**
     * Sets the amount of gas to use for the call.
     * <p>
     * All of the gas offered will be charged for.
     *
     * @return {@code this}
     * @param gas The long to be set as gas
     */
    public ContractCallQuery setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // network bug: ContractCallLocal cost estimate is too low
        return super.getCostAsync(client).thenApply(cost -> Hbar.fromTinybars((long) (cost.toTinybars() * 1.1)));
    }

    public ByteString getFunctionParameters() {
      return builder.getFunctionParameters();
    }
    /**
     * Sets the function parameters as their raw bytes.
     * <p>
     * Use this instead of {@link #setFunction(String, ContractFunctionParameters)} if you have already
     * pre-encoded a solidity function call.
     *
     * @return {@code this}
     * @param functionParameters The function parameters to be set
     */
    public ContractCallQuery setFunctionParameters(byte[] functionParameters) {
        builder.setFunctionParameters(ByteString.copyFrom(functionParameters));
        return this;
    }

    /**
     * Sets the function name to call.
     * <p>
     * The function will be called with no parameters. Use {@link #setFunction(String, ContractFunctionParameters)}
     * to call a function with parameters.
     *
     * @return {@code this}
     * @param name The function name to be set
     */
    public ContractCallQuery setFunction(String name) {
        return setFunction(name, new ContractFunctionParameters());
    }

    /**
     * Sets the function to call, and the parameters to pass to the function.
     *
     * @return {@code this}
     * @param name The function name to be set
     * @param params The parameters to pass
     */
    public ContractCallQuery setFunction(String name, ContractFunctionParameters params) {
        Objects.requireNonNull(params);
        builder.setFunctionParameters(params.toBytes(name));
        return this;
    }

    /**
     * Sets the max number of bytes that the result might include.
     * The run will fail if it would have returned more than this number of bytes.
     *
     * @return {@code this}
     * @param size The long to be set as size
     */
    public ContractCallQuery setMaxResultSize(long size) {
        builder.setMaxResultSize(size);
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

        queryBuilder.setContractCallLocal(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getContractCallLocal().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getContractCallLocal().getHeader();
    }

    @Override
    ContractFunctionResult mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return new ContractFunctionResult(response.getContractCallLocal().getFunctionResult());
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getContractCallLocalMethodMethod();
    }
}
