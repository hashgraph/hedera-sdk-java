package com.hedera.hashgraph.sdk;

import com.google.common.annotations.VisibleForTesting;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import java8.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import io.grpc.Status.Code;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.regex.Pattern;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

abstract class Executable<SdkRequestT, ProtoRequestT, ResponseT, O> implements WithExecute<O> {
    static final Pattern RST_STREAM = Pattern
        .compile(".*\\brst[^0-9a-zA-Z]stream\\b.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Nullable
    protected Integer maxAttempts = null;

    @Nullable
    protected Duration maxBackoff = null;

    @Nullable
    protected Duration minBackoff = null;

    protected LockableList<AccountId> nodeAccountIds = new LockableList<>();
    protected List<Node> nodes = new ArrayList<>();

    protected boolean attemptedAllNodes = false;

    // Lambda responsible for executing synchronous gRPC requests. Pluggable for unit testing.
    @VisibleForTesting
    Function<GrpcRequest, ResponseT> blockingUnaryCall =
        (grpcRequest) -> ClientCalls.blockingUnaryCall(grpcRequest.createCall(), grpcRequest.getRequest());

    @Nullable
    protected Duration grpcDeadline;

    Executable() {
    }

    @Nullable
    public final Duration grpcDeadline() {
        return grpcDeadline;
    }

    public final SdkRequestT setGrpcDeadline(Duration grpcDeadline) {
        this.grpcDeadline = Objects.requireNonNull(grpcDeadline);

        // noinspection unchecked
        return (SdkRequestT) this;
    }

    /**
     * The maximum amount of time to wait between retries
     *
     * @return maxBackoff
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
     * The minimum amount of time to wait between retries
     *
     * @return minBackoff
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
            return new ArrayList<>(nodeAccountIds.getList());
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
        this.nodeAccountIds.setList(nodeAccountIds).setLocked(true);

        // noinspection unchecked
        return (SdkRequestT) this;
    }

    void checkNodeAccountIds() {
        if (nodeAccountIds.isEmpty()) {
            throw new IllegalStateException("Request node account IDs were not set before executing");
        }
    }

    abstract void onExecute(Client client) throws TimeoutException, PrecheckStatusException;

    abstract CompletableFuture<Void> onExecuteAsync(Client client);

    void mergeFromClient(Client client) {
        if (maxAttempts == null) {
            maxAttempts = client.getMaxAttempts();
        }

        if (maxBackoff == null) {
            maxBackoff = client.getMaxBackoff();
        }

        if (minBackoff == null) {
            minBackoff = client.getMinBackoff();
        }
    }

    private void delay(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public O execute(Client client) throws TimeoutException, PrecheckStatusException {
        return execute(client, client.getRequestTimeout());
    }

    @Override
    public O execute(Client client, Duration timeout) throws TimeoutException, PrecheckStatusException {
        Throwable lastException = null;

        mergeFromClient(client);
        onExecute(client);
        checkNodeAccountIds();
        setNodesFromNodeAccountIds(client);

        var timeoutTime = Instant.now().plus(timeout);

        for (int attempt = 1; /* condition is done within loop */; attempt++) {
            if (attempt > maxAttempts) {
                throw new MaxAttemptsExceededException(lastException);
            }

            if (Instant.now().isAfter(timeoutTime)) {
                throw new TimeoutException();
            }

            GrpcRequest grpcRequest = new GrpcRequest(client.network, attempt);
            Node node = grpcRequest.getNode();
            ResponseT response = null;

            // If we get an unhealthy node here, we've cycled through all the "good" nodes that have failed
            // and have no choice but to try a bad one.
            if (!node.isHealthy()) {
                delay(node.getRemainingTimeForBackoff());
            }

            if (node.channelFailedToConnect()) {
                logger.trace("Failed to connect channel for node {} for request #{}", node.getAccountId(), attempt);
                lastException = grpcRequest.reactToConnectionFailure();
                continue;
            }

            try {
                response = blockingUnaryCall.apply(grpcRequest);
            } catch (Throwable e) {
                lastException = e;
            }

            if (response == null) {
                if(grpcRequest.shouldRetryExceptionally(lastException)) {
                    continue;
                } else {
                    throw new RuntimeException(lastException);
                }
            }

            switch (grpcRequest.getStatus(response)) {
                case ServerError:
                    lastException = grpcRequest.mapStatusException();
                    continue;
                case Retry:
                    // Response is not ready yet from server, need to wait.
                    lastException = grpcRequest.mapStatusException();
                    if (attempt < maxAttempts) {
                        delay(grpcRequest.getDelay());
                    }
                    continue;
                case RequestError:
                    throw grpcRequest.mapStatusException();
                case Success:
                default:
                    return grpcRequest.mapResponse();
            }
        }
    }

    @Override
    @FunctionalExecutable
    public CompletableFuture<O> executeAsync(Client client) {
        mergeFromClient(client);

        return onExecuteAsync(client).thenCompose((v) -> {
            checkNodeAccountIds();
            setNodesFromNodeAccountIds(client);

            return executeAsync(client, 1, null);
        })
            .orTimeout(client.getRequestTimeout().toMillis(), TimeUnit.MILLISECONDS);
    }

    @VisibleForTesting
    void setNodesFromNodeAccountIds(Client client) {
        for (var accountId : nodeAccountIds) {
            @Nullable
            var node = client.network.getNode(accountId);
            if (node == null) {
                throw new IllegalStateException("Some node account IDs did not map to valid nodes in the client's network");
            }
            nodes.add(Objects.requireNonNull(node));
        }
    }

    /**
     * Return the next node for execution. Will select the first node that is deemed healthy.
     * If we cannot find such a node and have tried n nodes (n being the size of the node list), we will
     * select the node with the smallest remaining delay. All delays MUST be executed in calling layer
     * as this method will be called for sync + async scenarios.
     */
    @VisibleForTesting
    Node getNodeForExecute(int attempt) {
        Node node = null;
        Node candidate = null;
        long smallestDelay = Long.MAX_VALUE;

        for (int i = 0; i < nodes.size(); i++) {
            node = nodes.get(nodeAccountIds.getIndex());

            if (!node.isHealthy()) {
                // Keep track of the node with the smallest delay seen thus far. If we go through the entire list
                // (meaning all nodes are unhealthy) then we will select the node with the smallest delay.
                long backoff = node.getRemainingTimeForBackoff();
                if (backoff < smallestDelay) {
                    candidate = node;
                    smallestDelay = backoff;
                }

                node = null;
                advanceRequest();
            } else {
                break; // got a good node, use it
            }
        }

        if (node == null) {
            node = candidate;

            // If we've tried all nodes, index will be +1 too far. Index increment happens outside
            // this method so try to be consistent with happy path.
            nodeAccountIds.setIndex(Math.max(0, nodeAccountIds.getIndex()));
        }

        // node won't be null at this point because execute() validates before this method is called.
        // Add null check here to work around sonar NPE detection.
        if (node != null)
            logger.trace("Using node {} for request #{}: {}", node.getAccountId(), attempt, this);

        return node;
    }

    private ProtoRequestT getRequestForExecute() {
        var request = makeRequest();

        // advance the internal index
        // non-free queries and transactions map to more than 1 actual transaction and this will cause
        // the next invocation of makeRequest to return the _next_ transaction
        advanceRequest();

        return request;
    }

    private CompletableFuture<O> executeAsync(Client client, int attempt, @Nullable Throwable lastException) {
        if (attempt > maxAttempts) {
            return CompletableFuture.<O>failedFuture(new MaxAttemptsExceededException(lastException));
        }

        GrpcRequest grpcRequest = new GrpcRequest(client.network, attempt);

        // Sleeping if a node is not healthy should not increment attempt as we didn't really make an attempt
        if (!grpcRequest.getNode().isHealthy()) {
            return Delayer.delayFor(grpcRequest.getNode().getRemainingTimeForBackoff(), client.executor)
                .thenCompose((v) -> executeAsync(client, attempt, lastException));
        }

        return grpcRequest.getNode().channelFailedToConnectAsync().thenCompose(connectionFailed -> {
            if (connectionFailed) {
                var connectionException = grpcRequest.reactToConnectionFailure();
                return executeAsync(client, attempt + 1, connectionException);
            }

            return toCompletableFuture(ClientCalls.futureUnaryCall(grpcRequest.createCall(), grpcRequest.getRequest())).handle((response, error) -> {
                if (grpcRequest.shouldRetryExceptionally(error)) {
                    // the transaction had a network failure reaching Hedera
                    return executeAsync(client, attempt + 1, error);
                }

                if (error != null) {
                    // not a network failure, some other weirdness going on; just fail fast
                    return CompletableFuture.<O>failedFuture(error);
                }

                switch (grpcRequest.getStatus(response)) {
                    case ServerError:
                        return executeAsync(client, attempt + 1, grpcRequest.mapStatusException());
                    case Retry:
                        return Delayer.delayFor((attempt < maxAttempts) ? grpcRequest.getDelay() : 0, client.executor)
                            .thenCompose((v) -> executeAsync(client, attempt + 1, grpcRequest.mapStatusException()));
                    case RequestError:
                        return CompletableFuture.<O>failedFuture(grpcRequest.mapStatusException());
                    case Success:
                    default:
                        return CompletableFuture.completedFuture(grpcRequest.mapResponse());
                }
            }).thenCompose(x -> x);
        });
    }

    abstract ProtoRequestT makeRequest();

    GrpcRequest getGrpcRequest(int attempt) {
        return new GrpcRequest(null, attempt);
    }

    void advanceRequest() {
        if (nodeAccountIds.getIndex() + 1 == nodes.size() - 1) {
            attemptedAllNodes = true;
        }

        nodeAccountIds.advance();
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
    abstract TransactionId getTransactionIdInternal();

    boolean shouldRetryExceptionally(@Nullable Throwable error) {
        if (error instanceof StatusRuntimeException) {
            var statusException = (StatusRuntimeException) error;
            var status = statusException.getStatus().getCode();
            var description = statusException.getStatus().getDescription();

            return (status == Code.UNAVAILABLE) ||
                (status == Code.RESOURCE_EXHAUSTED) ||
                (status == Code.INTERNAL && description != null && RST_STREAM.matcher(description).matches());
        }

        return false;
    }

    /**
     * Default implementation, may be overridden in subclasses (especially for query case). Called just
     * after receiving the query response from Hedera. By default it triggers a retry when the pre-check
     * status is {@code BUSY}.
     */
    ExecutionState shouldRetry(Status status, ResponseT response) {
        switch (status) {
            case PLATFORM_TRANSACTION_NOT_CREATED:
            case PLATFORM_NOT_ACTIVE:
            case BUSY:
                return ExecutionState.ServerError;
            case OK:
                return ExecutionState.Success;
            default:
                return ExecutionState.RequestError;     // user error
        }
    }

    @VisibleForTesting
    class GrpcRequest {
        @Nullable
        private final Network network;
        private final Node node;
        private final int attempt;
        //private final ClientCall<ProtoRequestT, ResponseT> call;
        private final ProtoRequestT request;
        private final long startAt;
        private final long delay;

        private ResponseT response;
        private double latency;
        private Status responseStatus;

        GrpcRequest(@Nullable Network network, int attempt) {
            this.network = network;
            this.attempt = attempt;
            this.node = getNodeForExecute(attempt);
            this.request = getRequestForExecute();
            this.startAt = System.nanoTime();

            // Exponential back-off for Delayer: 250ms, 500ms, 1s, 2s, 4s, 8s, ... 8s
            delay = (long) Math.min(Objects.requireNonNull(minBackoff).toMillis() * Math.pow(2, attempt - 1), Objects.requireNonNull(maxBackoff).toMillis());
        }

        public CallOptions getCallOptions() {
            var options = CallOptions.DEFAULT;

            if (Executable.this.grpcDeadline != null) {
                return options.withDeadlineAfter(Executable.this.grpcDeadline.toMillis(), TimeUnit.MILLISECONDS);
            } else {
                return options;
            }
        }

        public Node getNode() {
            return node;
        }

        public ClientCall<ProtoRequestT, ResponseT> createCall() {
            verboseLog(node);
            return this.node.getChannel().newCall(Executable.this.getMethodDescriptor(), getCallOptions());
        }

        public ProtoRequestT getRequest() {
            return request;
        }

        public long getDelay() {
            return delay;
        }

        Throwable reactToConnectionFailure() {
            Objects.requireNonNull(network).increaseBackoff(node);
            logger.warn("Retrying node {} in {} ms after channel connection failure during attempt #{}",
                node.getAccountId(), node.getRemainingTimeForBackoff(), attempt);
            verboseLog(node);
            return new IllegalStateException("Failed to connect to node " + node.getAccountId());
        }

        boolean shouldRetryExceptionally(@Nullable Throwable e) {
            latency = (double) (System.nanoTime() - startAt) / 1000000000.0;

            var retry = Executable.this.shouldRetryExceptionally(e);

            if (retry) {
                Objects.requireNonNull(network).increaseBackoff(node);
                logger.warn("Retrying node {} in {} ms after failure during attempt #{}: {}",
                    node.getAccountId(), node.getRemainingTimeForBackoff(), attempt, e != null ? e.getMessage() : "NULL");
                verboseLog(node);
            }

            return retry;
        }

        PrecheckStatusException mapStatusException() {
            // request to hedera failed in a non-recoverable way
            return new PrecheckStatusException(responseStatus, Executable.this.getTransactionIdInternal());
        }

        O mapResponse() {
            // successful response from Hedera
            return Executable.this.mapResponse(response, node.getAccountId(), request);
        }

        ExecutionState getStatus(ResponseT response) {
            node.decreaseBackoff();

            this.response = response;
            this.responseStatus = Executable.this.mapResponseStatus(response);

            logger.trace("Received {} response in {} s from node {} during attempt #{}: {}",
                responseStatus, latency, node.getAccountId(), attempt, response);

            // Delegate interpretation of response status to subclass. Queries will initiate retries
            // differently from transaction submissions.
            var executionState = Executable.this.shouldRetry(responseStatus, response);
            if (executionState == ExecutionState.ServerError && attemptedAllNodes) {
                executionState = ExecutionState.Retry;
                attemptedAllNodes = false;
            }
            switch (executionState) {
                case Retry:
                    logger.warn("Retrying node {} in {} ms after failure during attempt #{}: {}",
                        node.getAccountId(), delay, attempt, responseStatus);
                    verboseLog(node);
                    break;
                case ServerError:
                    logger.warn("Problem submitting request to node {} for attempt #{}, retry with new node: {}",
                        node.getAccountId(), attempt, responseStatus);
                    break;
                default:
                    // Do nothing
            }

            return executionState;
        }

        void verboseLog(Node node) {
            String ipAddress;
            if (node.address == null) {
                ipAddress = "NULL";
            } else if (node.address.getAddress() == null) {
                ipAddress = "NULL";
            } else {
                ipAddress = node.address.getAddress();
            }
            logger.trace("Node IP {} Timestamp {} Transaction Type {}",
                ipAddress,
                System.currentTimeMillis(),
                this.getClass() != null ? this.getClass().getSimpleName() : "NULL"
            );
        }
    }
}
