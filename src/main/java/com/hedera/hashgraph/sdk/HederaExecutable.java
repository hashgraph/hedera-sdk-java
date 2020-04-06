package com.hedera.hashgraph.sdk;

import io.grpc.CallOptions;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import java8.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

public abstract class HederaExecutable<RequestT, ResponseT, O> extends Executable<O> {
    HederaExecutable() {
    }

    @Override
    public CompletableFuture<O> executeAsync(Client client) {
        return executeAsync(client, 1);
    }

    private CompletableFuture<O> executeAsync(Client client, int attempt) {
        var nodeId = getNodeId(client);
        var channel = client.getChannel(nodeId);
        var methodDescriptor = getMethodDescriptor();
        var call = channel.newCall(methodDescriptor, CallOptions.DEFAULT);
        var request = makeRequest();

        return toCompletableFuture(ClientCalls.futureUnaryCall(call, request)).handle((response, error) -> {
            if (shouldRetryExceptionally(error)) {
                // the transaction had a network failure reaching Hedera
                return Delayer.delayBackOff(attempt, client.executor)
                    .thenCompose((v) -> executeAsync(client, attempt + 1));
            }

            if (error != null) {
                // not a network failure, some other weirdness going on; just fail
                // fast
                return CompletableFuture.<O>failedFuture(error);
            }

            var responseStatus = mapResponseStatus(response);

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

    protected abstract RequestT makeRequest();

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
}
