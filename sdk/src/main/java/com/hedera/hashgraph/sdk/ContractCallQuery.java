package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractCallLocalQuery;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import java.util.Objects;

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
    @Nullable
    private ContractId contractId = null;
    private long gas = 0;
    private byte[] functionParameters = {};
    private long maxResultSize = 0;

    public ContractCallQuery() {
    }

    @Nullable
    public ContractId getContractId() {
        return contractId;
    }

    /**
     * Sets the contract instance to call, in the format used in transactions.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public ContractCallQuery setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        this.contractId = contractId;
        return this;
    }

    public long getGas() {
        return gas;
    }

    /**
     * Sets the amount of gas to use for the call.
     * <p>
     * All of the gas offered will be charged for.
     *
     * @param gas The long to be set as gas
     * @return {@code this}
     */
    public ContractCallQuery setGas(long gas) {
        this.gas = gas;
        return this;
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // network bug: ContractCallLocal cost estimate is too low
        return super.getCostAsync(client).thenApply(cost -> Hbar.fromTinybars((long) (cost.toTinybars() * 1.1)));
    }

    public ByteString getFunctionParameters() {
        return ByteString.copyFrom(functionParameters);
    }

    /**
     * Sets the function parameters as their raw bytes.
     * <p>
     * Use this instead of {@link #setFunction(String, ContractFunctionParameters)} if you have already
     * pre-encoded a solidity function call.
     *
     * @param functionParameters The function parameters to be set
     * @return {@code this}
     */
    public ContractCallQuery setFunctionParameters(byte[] functionParameters) {
        this.functionParameters = functionParameters;
        return this;
    }

    /**
     * Sets the function name to call.
     * <p>
     * The function will be called with no parameters. Use {@link #setFunction(String, ContractFunctionParameters)}
     * to call a function with parameters.
     *
     * @param name The function name to be set
     * @return {@code this}
     */
    public ContractCallQuery setFunction(String name) {
        return setFunction(name, new ContractFunctionParameters());
    }

    /**
     * Sets the function to call, and the parameters to pass to the function.
     *
     * @param name   The function name to be set
     * @param params The parameters to pass
     * @return {@code this}
     */
    public ContractCallQuery setFunction(String name, ContractFunctionParameters params) {
        Objects.requireNonNull(params);
        setFunctionParameters(params.toBytes(name).toByteArray());
        return this;
    }

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    public long getMaxResultSize() {
        return maxResultSize;
    }

    /**
     * @deprecated with no replacement
     *
     * Sets the max number of bytes that the result might include.
     * The run will fail if it would have returned more than this number of bytes.
     *
     * @param size The long to be set as size
     * @return {@code this}
     */
    @Deprecated
    public ContractCallQuery setMaxResultSize(long size) {
        maxResultSize = size;
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (contractId != null) {
            contractId.validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = ContractCallLocalQuery.newBuilder();
        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }
        builder.setGas(gas);
        builder.setFunctionParameters(ByteString.copyFrom(functionParameters));

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
