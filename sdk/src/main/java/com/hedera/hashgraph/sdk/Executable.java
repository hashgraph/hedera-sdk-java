package com.hedera.hashgraph.sdk;

import io.grpc.CallOptions;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import java8.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

abstract class Executable<SdkRequestT, ProtoRequestT, ResponseT, O> implements WithExecute<O> {
    private static final Logger logger = LoggerFactory.getLogger(Executable.class);

    protected int maxAttempts = 10;
    protected int nextNodeIndex = 0;
    protected List<AccountId> nodeAccountIds = Collections.emptyList();
    protected List<Node> nodes = new ArrayList<>();

    Executable() {
    }

    /**
     * @deprecated Use {@link #getMaxAttempts()} instead.
     */
    @java.lang.Deprecated
    public final int getMaxRetry() {
        return getMaxAttempts();
    }

    /**
     * @deprecated Use {@link #setMaxAttempts(int)} instead.
     */
    @java.lang.Deprecated
    public final SdkRequestT setMaxRetry(int count) {
        return setMaxAttempts(count);
    }

    public final int getMaxAttempts() {
        return maxAttempts;
    }

    public final SdkRequestT setMaxAttempts(int count) {
        maxAttempts = count;
        // noinspection unchecked
        return (SdkRequestT) this;
    }

    @Nullable
    public final List<AccountId> getNodeAccountIds() {
        if (!nodeAccountIds.isEmpty()) {
            return nodeAccountIds;
        }

        return null;
    }

    /**
     * Set the account IDs of the nodes that this transaction will be submitted to.
     * <p>
     * Providing an explicit node account ID interferes with client-side load balancing of the
     * network. By default, the SDK will pre-generate a transaction for 1/3 of the nodes on the
     * network. If a node is down, busy, or otherwise reports a fatal error, the SDK will try again
     * with a different node.
     *
     * @param nodeAccountIds The list of node AccountIds to be set
     * @return {@code this}
     */
    public SdkRequestT setNodeAccountIds(List<AccountId> nodeAccountIds) {
        this.nodeAccountIds = nodeAccountIds;

        // noinspection unchecked
        return (SdkRequestT) this;
    }

    abstract CompletableFuture<Void> onExecuteAsync(Client client);

    @Override
    @FunctionalExecutable
    public CompletableFuture<O> executeAsync(Client client) {
        return onExecuteAsync(client).thenCompose((v) -> {
            if(nodeAccountIds.isEmpty()) {
                throw new IllegalStateException("Request node account IDs were not set before executing");
            }

            setNodesFromNodeAccountIds(client);

            return executeAsync(client, 1, null);
        });
    }

    private void setNodesFromNodeAccountIds(Client client) {
        for(var accountId : nodeAccountIds) {
            @Nullable
            var node = client.network.networkNodes.get(accountId);
            if(node == null) {
                throw new IllegalStateException("Some node account IDs did not map to valid nodes in the client's network");
            }
            nodes.add(Objects.requireNonNull(node));
        }
    }

    private CompletableFuture<O> executeAsync(Client client, int attempt, @Nullable Throwable lastException) {
        if (attempt > maxAttempts) {
            return CompletableFuture.<O>failedFuture(new Exception("Failed to get gRPC response within maximum retry count", lastException));
        }

        var node = nodes.get(nextNodeIndex);
        node.inUse();

        logger.trace("Sending request #{} to node {}: {}", attempt, node.accountId, this);

        if (!node.isHealthy()) {
            logger.warn("Using unhealthy node {}. Delaying attempt #{} for {} ms", node.accountId, attempt, node.delayUntil);

            return Delayer.delayFor(node.delay(), client.executor)
                .thenCompose((v) -> executeAsync(client, attempt + 1, lastException));
        }

        var methodDescriptor = getMethodDescriptor();
        var call = node.getChannel().newCall(methodDescriptor, CallOptions.DEFAULT);
        var request = makeRequest();

        // advance the internal index
        // non-free queries and transactions map to more than 1 actual transaction and this will cause
        // the next invocation of makeRequest to return the _next_ transaction
        advanceRequest();

        var startAt = System.nanoTime();

        return toCompletableFuture(ClientCalls.futureUnaryCall(call, request)).handle((response, error) -> {
            var latency = (double) (System.nanoTime() - startAt) / 1000000000.0;

            // Exponential back-off for Delayer: 250ms, 500ms, 1s, 2s, 4s, 8s, ... 8s
            long delay = (long) Math.min(250 * Math.pow(2, attempt - 1), 8000);

            if (shouldRetryExceptionally(error)) {
                logger.warn("Retrying node {} in {} ms after failure during attempt #{}: {}",
                    node.accountId, delay, attempt, error.getMessage());
                node.increaseDelay();

                // the transaction had a network failure reaching Hedera
                return executeAsync(client, attempt + 1, error);
            }

            if (error != null) {
                // not a network failure, some other weirdness going on; just fail fast
                return CompletableFuture.<O>failedFuture(error);
            }

            node.decreaseDelay();

            var responseStatus = mapResponseStatus(response);

            logger.trace("Received {} response in {} s from node {} during attempt #{}: {}",
                responseStatus, latency, node.accountId, attempt, response);

            switch (shouldRetry(responseStatus, response)) {
                case Retry:
                    // the response has been identified as failing or otherwise
                    // needing a retry let's do this again after a delay
                    logger.warn("Retrying node {} in {} ms after failure during attempt #{}: {}",
                        node.accountId, delay, attempt, responseStatus);
                    return Delayer.delayFor(delay, client.executor)
                        .thenCompose(
                            (v) -> executeAsync(
                                client,
                                attempt + 1,
                                new PrecheckStatusException(responseStatus, getTransactionId())
                            )
                        );

                case Error:
                    // request to hedera failed in a non-recoverable way
                    return CompletableFuture.<O>failedFuture(
                        mapStatusError(responseStatus,
                            getTransactionId(),
                            response,
                            client.network.networkName
                        )
                    );

                case Finished:
                default:
                    // successful response from Hedera
                    return CompletableFuture.completedFuture(mapResponse(response, node.accountId, request, client.network.networkName));
            }
        }).thenCompose(x -> x);
    }

    abstract ProtoRequestT makeRequest();

    void advanceRequest() {
        // each time buildNext is called we move our cursor to the next transaction
        // wrapping around to ensure we are cycling
        nextNodeIndex = (nextNodeIndex + 1) % nodeAccountIds.size();
    }

    /**
     * Called after receiving the query response from Hedera. The derived class should map into its
     * output type.
     */
    abstract O mapResponse(ResponseT response, AccountId nodeId, ProtoRequestT request, @Nullable NetworkName networkName);

    abstract Status mapResponseStatus(ResponseT response);

    /**
     * Called to direct the invocation of the query to the appropriate gRPC service.
     */
    abstract MethodDescriptor<ProtoRequestT, ResponseT> getMethodDescriptor();

    @Nullable
    abstract TransactionId getTransactionId();

    boolean shouldRetryExceptionally(@Nullable Throwable error) {
        if (error instanceof StatusRuntimeException) {
            var status = ((StatusRuntimeException) error).getStatus().getCode();

            return status.equals(io.grpc.Status.UNAVAILABLE.getCode())
                || status.equals(io.grpc.Status.RESOURCE_EXHAUSTED.getCode());
        }

        return false;
    }

    /**
     * Called just after receiving the query response from Hedera. By default it triggers a retry
     * when the pre-check status is {@code BUSY}.
     */
    ExecutionState shouldRetry(Status status, ResponseT response) {
        switch (status) {
            case PLATFORM_TRANSACTION_NOT_CREATED:
            case BUSY:
                return ExecutionState.Retry;
            case OK:
                return ExecutionState.Finished;
            default:
                return ExecutionState.Error;
        }
    }

    Exception mapStatusError(Status status, @Nullable TransactionId transactionId, ResponseT response, @Nullable NetworkName networkName) {
        return new PrecheckStatusException(status, transactionId);
    }
}
