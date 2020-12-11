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
import java.util.List;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

abstract class Executable<SdkRequestT, ProtoRequestT, ResponseT, O> implements WithExecute<O> {
    private static final Logger logger = LoggerFactory.getLogger(Executable.class);

    protected int maxRetries = 10;
    protected int nextNodeIndex = 0;
    protected List<AccountId> nodeAccountIds = Collections.emptyList();

    Executable() {
    }

    public final int getMaxRetry() {
        return maxRetries;
    }

    public final SdkRequestT setMaxRetry(int count) {
        maxRetries = count;
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

    @FunctionalExecutable
    public CompletableFuture<O> executeAsync(Client client) {
        return onExecuteAsync(client).thenCompose((v) -> executeAsync(client, 1, null));
    }

    private CompletableFuture<O> executeAsync(Client client, int attempt, @Nullable Throwable lastException) {
        if (attempt > maxRetries) {
            return CompletableFuture.<O>failedFuture(new Exception("Failed to get gRPC response within maximum retry count", lastException));
        }

        var node = client.network.networkNodes.get(getNodeAccountId());
        node.inUse();

        logger.trace("sending request \nnode={}\nattempt={}\n{}", node.accountId, attempt, this);

        if (!node.isHealthy()) {
            logger.error("using unhealthy node={}\ndelaying until {}ms\nattempt={}\n",
                node.accountId,
                node.delayUntil,
                attempt
            );

            return Delayer.delayFor(node.delay, client.executor)
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

            // Exponential back-off for Delayer: 250ms, 500ms, 1s, 2s, 4s, 8s, 16s, ...16s
            long delay = (long) Math.min(250 * Math.pow(2, attempt - 1), 16000);

            if (shouldRetryExceptionally(error)) {
                logger.error("caught error, retrying\nnode={}\nattempt={}\n{}",
                    node.accountId,
                    attempt,
                    error
                );

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

            logger.trace("received response in {}s\nnode={}\nattempt={}\nstatus={}\n{}",
                latency,
                node.accountId,
                attempt,
                responseStatus,
                response
            );

            if (shouldRetry(responseStatus, response)) {
                // the response has been identified as failing or otherwise
                // needing a retry let's do this again after a delay
                return Delayer.delayFor(delay, client.executor)
                    .thenCompose((v) -> executeAsync(client, attempt + 1, new HederaPreCheckStatusException(responseStatus, getTransactionId())));
            }

            if (responseStatus != Status.OK) {
                // request to hedera failed in a non-recoverable way
                return CompletableFuture.<O>failedFuture(
                    new HederaPreCheckStatusException(
                        responseStatus, getTransactionId()));
            }

            // successful response from Hedera
            return CompletableFuture.completedFuture(mapResponse(response, node.accountId, request));
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
    abstract O mapResponse(ResponseT response, AccountId NodeId, ProtoRequestT request);

    abstract Status mapResponseStatus(ResponseT response);

    /**
     * Called to direct the invocation of the query to the appropriate gRPC service.
     */
    abstract MethodDescriptor<ProtoRequestT, ResponseT> getMethodDescriptor();

    final AccountId getNodeAccountId() {
        if (!nodeAccountIds.isEmpty()) {
            return nodeAccountIds.get(nextNodeIndex);
        } else {
            throw new IllegalStateException("Request node account IDs were not set before executing");
        }
    }

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
    boolean shouldRetry(Status status, ResponseT response) {
        if(status == Status.PLATFORM_TRANSACTION_NOT_CREATED){
            return true;
        }else{
            return status == Status.BUSY;
        }
    }
}
