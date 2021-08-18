package com.hedera.hashgraph.sdk;

import io.grpc.CallOptions;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import java8.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

abstract class Executable<SdkRequestT, ProtoRequestT, ResponseT, O> implements WithExecute<O> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    static final Pattern RST_STREAM = Pattern
        .compile(".*\\brst[^0-9a-zA-Z]stream\\b.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    protected Integer maxAttempts;
    protected Duration maxBackoff;
    protected Duration minBackoff;
    protected int nextNodeIndex = 0;
    protected List<AccountId> nodeAccountIds = Collections.emptyList();
    protected List<Node> nodes = new ArrayList<>();

    Executable() {
    }

    /**
     * @return maxBackoff The maximum amount of time to wait between retries
     */
    public final Duration getMaxBackoff() {
        return maxBackoff != null ? maxBackoff : Client.DEFAULT_MAX_BACKOFF;
    }

    /**
     * The maximum amount of time to wait between retries. Every retry attempt will increase the wait time exponentially
     * until it reaches this time.
     *
     * @param maxBackoff The maximum amount of time to wait between retries
     * @return {@code this}
     */
    public final SdkRequestT setMaxBackoff(Duration maxBackoff) {
        if (maxBackoff == null || maxBackoff.toNanos() < 0) {
            throw new IllegalArgumentException("maxBackoff must be a positive duration");
        } else if (maxBackoff.compareTo(getMinBackoff()) < 0) {
            throw new IllegalArgumentException("maxBackoff must be greater than or equal to minBackoff");
        }
        this.maxBackoff = maxBackoff;
        // noinspection unchecked
        return (SdkRequestT) this;
    }

    /**
     * @return minBackoff The minimum amount of time to wait between retries
     */
    public final Duration getMinBackoff() {
        return minBackoff != null ? minBackoff : Client.DEFAULT_MIN_BACKOFF;
    }

    /**
     * The minimum amount of time to wait between retries. When retrying, the delay will start at this time and increase
     * exponentially until it reaches the maxBackoff.
     *
     * @param minBackoff The minimum amount of time to wait between retries
     * @return {@code this}
     */
    public final SdkRequestT setMinBackoff(Duration minBackoff) {
        if (minBackoff == null || minBackoff.toNanos() < 0) {
            throw new IllegalArgumentException("minBackoff must be a positive duration");
        } else if (minBackoff.compareTo(getMaxBackoff()) > 0) {
            throw new IllegalArgumentException("minBackoff must be less than or equal to maxBackoff");
        }
        this.minBackoff = minBackoff;
        // noinspection unchecked
        return (SdkRequestT) this;
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
        return maxAttempts != null ? maxAttempts : Client.DEFAULT_MAX_ATTEMPTS;
    }

    public final SdkRequestT setMaxAttempts(int maxAttempts) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be greater than zero");
        }
        this.maxAttempts = maxAttempts;
        // noinspection unchecked
        return (SdkRequestT) this;
    }

    @Nullable
    public final List<AccountId> getNodeAccountIds() {
        if (!nodeAccountIds.isEmpty()) {
            return new ArrayList<>(nodeAccountIds);
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
        this.nodeAccountIds = new ArrayList<>(nodeAccountIds);

        // noinspection unchecked
        return (SdkRequestT) this;
    }

    abstract CompletableFuture<Void> onExecuteAsync(Client client);

    @Override
    @FunctionalExecutable
    public CompletableFuture<O> executeAsync(Client client) {
        if (maxAttempts == null) {
            maxAttempts = client.getMaxAttempts();
        }

        if (maxBackoff == null) {
            maxBackoff = client.getMaxBackoff();
        }

        if (minBackoff == null) {
            minBackoff = client.getMinBackoff();
        }

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
            long delay = (long) Math.min(minBackoff.toMillis() * Math.pow(2, attempt - 1), maxBackoff.toMillis());

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
                            response
                        )
                    );

                case Finished:
                default:
                    // successful response from Hedera
                    return CompletableFuture.completedFuture(mapResponse(response, node.accountId, request));
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
    abstract O mapResponse(ResponseT response, AccountId nodeId, ProtoRequestT request);

    abstract Status mapResponseStatus(ResponseT response);

    /**
     * Called to direct the invocation of the query to the appropriate gRPC service.
     */
    abstract MethodDescriptor<ProtoRequestT, ResponseT> getMethodDescriptor();

    @Nullable
    abstract TransactionId getTransactionId();

    boolean shouldRetryExceptionally(@Nullable Throwable error) {
        if (error instanceof StatusRuntimeException) {
            var statusException = (StatusRuntimeException) error;
            var status = statusException.getStatus().getCode();
            var description = statusException.getStatus().getDescription();

            return (status == io.grpc.Status.Code.UNAVAILABLE) ||
                (status == io.grpc.Status.Code.RESOURCE_EXHAUSTED) ||
                (status == io.grpc.Status.Code.INTERNAL && description != null && RST_STREAM.matcher(description).matches());
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

    Exception mapStatusError(Status status, @Nullable TransactionId transactionId, ResponseT response) {
        return new PrecheckStatusException(status, transactionId);
    }
}
