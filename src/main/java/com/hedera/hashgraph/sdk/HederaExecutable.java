package com.hedera.hashgraph.sdk;

import io.grpc.CallOptions;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.concurrent.TimeoutException;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

public abstract class HederaExecutable<RequestT, ResponseT, O> extends Executable<O> {
    private static final Logger logger = LoggerFactory.getLogger(HederaExecutable.class);

    HederaExecutable() {
    }

    protected abstract CompletableFuture<Void> onExecuteAsync(Client client);

    @Override
    public final CompletableFuture<O> executeAsync(Client client) {
        return onExecuteAsync(client).thenCompose((v) -> executeAsync(client, 1));
    }

    private CompletableFuture<O> executeAsync(Client client, int attempt) {
        var nodeId = getNodeId(client);
        var channel = client.getChannel(nodeId);
        var methodDescriptor = getMethodDescriptor();
        var call = channel.newCall(methodDescriptor, CallOptions.DEFAULT);
        var request = makeRequest(client);
        var startAt = System.nanoTime();

        logger.atTrace()
            .addKeyValue("node", nodeId)
            .addKeyValue("attempt", attempt)
            .log("sending request \n{}", debugToString(request));

        return toCompletableFuture(ClientCalls.futureUnaryCall(call, request)).handle((response, error) -> {
            var latency = (double) (System.nanoTime() - startAt) / 1000000000.0;

            if (shouldRetryExceptionally(error)) {
                logger.atError()
                    .addKeyValue("node", nodeId)
                    .addKeyValue("attempt", attempt)
                    .setCause(error)
                    .log("caught error, retrying");

                // the transaction had a network failure reaching Hedera
                return Delayer.delayBackOff(attempt, client.executor)
                    .thenCompose((v) -> executeAsync(client, attempt + 1));
            }

            if (error != null) {
                // not a network failure, some other weirdness going on; just fail fast
                return CompletableFuture.<O>failedFuture(error);
            }

            var responseStatus = mapResponseStatus(response);

            logger.atTrace()
                .addKeyValue("node", nodeId)
                .addKeyValue("attempt", attempt)
                .addKeyValue("status", responseStatus)
                .log("received response in {}s\n{}", latency, response);

            if (shouldRetry(responseStatus, response)) {
                // the response has been identified as failing or otherwise
                // needing a retry let's do this again after a delay
                return Delayer.delayBackOff(attempt, client.executor)
                    .thenCompose((v) -> executeAsync(client, attempt + 1));
            }

            if (responseStatus != Status.Ok) {
                // request to hedera failed in a non-recoverable way
                return CompletableFuture.<O>failedFuture(
                    new HederaPreCheckStatusException(
                        responseStatus, getTransactionId()));
            }

            // successful response from Hedera
            return CompletableFuture.completedFuture(mapResponse(response));
        }).thenCompose(x -> x);
    }

    protected abstract Executable<Hbar> getCostExecutable(Client client);

    public CompletableFuture<Hbar> getCostAsync(Client client) {
        return getCostExecutable(client).executeAsync(client);
    }

    public void getCostAsync(Client client, BiConsumer<Hbar, Throwable> callback) {
        getCostExecutable(client).executeAsync(client, callback);
    }

    public void getCostAsync(Client client, Consumer<Hbar> onSuccess, Consumer<Throwable> onFailure) {
        getCostExecutable(client).executeAsync(client, onSuccess, onFailure);
    }

    @SuppressWarnings("InconsistentOverloads")
    public void getCostAsync(Client client, Duration timeout, BiConsumer<Hbar, Throwable> callback) {
        getCostExecutable(client).executeAsync(client, timeout, callback);
    }

    @SuppressWarnings("InconsistentOverloads")
    public void getCostAsync(Client client, Duration timeout, Consumer<Hbar> onSuccess, Consumer<Throwable> onFailure) {
        getCostExecutable(client).executeAsync(client, timeout, onSuccess, onFailure);
    }

    public Hbar getCost(Client client) throws TimeoutException {
        return getCostExecutable(client).execute(client);
    }

    public Hbar getCost(Client client, Duration timeout) throws TimeoutException {
        return getCostExecutable(client).execute(client, timeout);
    }

    protected abstract RequestT makeRequest(Client client);

    /**
     * Called after receiving the query response from Hedera. The derived class should map into its
     * output type.
     */
    protected abstract O mapResponse(ResponseT response);

    protected abstract Status mapResponseStatus(ResponseT response);

    /**
     * Called to direct the invocation of the query to the appropriate gRPC service.
     */
    protected abstract MethodDescriptor<RequestT, ResponseT> getMethodDescriptor();

    protected abstract AccountId getNodeId(Client client);

    protected abstract TransactionId getTransactionId();

    protected boolean shouldRetryExceptionally(@Nullable Throwable error) {
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
    protected boolean shouldRetry(Status status, ResponseT response) {
        return status == Status.Busy;
    }

    protected String debugToString(RequestT request) {
        return request.toString();
    }
}
