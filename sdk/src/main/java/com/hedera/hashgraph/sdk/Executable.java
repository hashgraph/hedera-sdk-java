package com.hedera.hashgraph.sdk;

import io.grpc.CallOptions;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import java8.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

abstract class Executable<RequestT, ResponseT, O> implements WithExecute<O> {
    private static final Logger logger = LoggerFactory.getLogger(Executable.class);

    Executable() {
    }

    abstract CompletableFuture<Void> onExecuteAsync(Client client);

    @FunctionalExecutable
    public CompletableFuture<O> executeAsync(Client client) {
        return onExecuteAsync(client).thenCompose((v) -> executeAsync(client, 1));
    }

    private CompletableFuture<O> executeAsync(Client client, int attempt) {
        var nodeId = getNodeAccountId();

        logger.atTrace()
            .addKeyValue("node", nodeId)
            .addKeyValue("attempt", attempt)
            .log("sending request \n{}", this);

        var channel = client.getNetworkChannel(nodeId);
        var methodDescriptor = getMethodDescriptor();
        var call = channel.newCall(methodDescriptor, CallOptions.DEFAULT);
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
                logger.atError()
                    .addKeyValue("node", nodeId)
                    .addKeyValue("attempt", attempt)
                    .addKeyValue("delay:", delay)
                    .setCause(error)
                    .log("caught error, retrying");

                // the transaction had a network failure reaching Hedera
                return Delayer.delayFor(delay, client.executor)
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
                return Delayer.delayFor(delay, client.executor)
                    .thenCompose((v) -> executeAsync(client, attempt + 1));
            }

            if (responseStatus != Status.OK) {
                // request to hedera failed in a non-recoverable way
                return CompletableFuture.<O>failedFuture(
                    new HederaPreCheckStatusException(
                        responseStatus, getTransactionId()));
            }

            // successful response from Hedera
            return CompletableFuture.completedFuture(mapResponse(response, nodeId, request));
        }).thenCompose(x -> x);
    }

    abstract RequestT makeRequest();

    abstract void advanceRequest();

    /**
     * Called after receiving the query response from Hedera. The derived class should map into its
     * output type.
     */
    abstract O mapResponse(ResponseT response, AccountId NodeId, RequestT request);

    abstract Status mapResponseStatus(ResponseT response);

    /**
     * Called to direct the invocation of the query to the appropriate gRPC service.
     */
    abstract MethodDescriptor<RequestT, ResponseT> getMethodDescriptor();

    abstract AccountId getNodeAccountId();

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
