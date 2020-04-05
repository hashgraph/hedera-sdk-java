package com.hedera.hashgraph.sdk;

import static com.hedera.hashgraph.sdk.FutureConverter.toCompletableFuture;

import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.ResponseType;
import io.grpc.CallOptions;
import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import org.threeten.bp.Duration;

public abstract class QueryBuilder<R, T extends QueryBuilder<R, T>> {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final Query.Builder builder;

    private final QueryHeader.Builder headerBuilder;

    QueryBuilder() {
        builder = Query.newBuilder();
        headerBuilder = QueryHeader.newBuilder();

        headerBuilder.setResponseType(ResponseType.ANSWER_ONLY);
    }

    public R execute(Client client) throws TimeoutException {
        return execute(client, DEFAULT_TIMEOUT);
    }

    public R execute(Client client, Duration timeout) throws TimeoutException {
        try {
            return executeAsync(client).get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            var cause = e.getCause();

            // If there is no cause, just re-throw
            if (cause == null) throw new RuntimeException(e);

            // TODO: For explicit errors we want to have as checked, we need to
            //       do instanceof checks and bridge that here

            // Unwrap and re-wrap as a RuntimeException
            throw new RuntimeException(cause);
        }
    }

    public CompletableFuture<R> executeAsync(Client client) {
        return executeAsync(client, 1);
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private CompletableFuture<R> executeAsync(Client client, int attempt) {
        var nodeId = client.getNextNodeId();
        var channel = client.getChannel(nodeId);
        var call = channel.newCall(getMethodDescriptor(), CallOptions.DEFAULT);

        return toCompletableFuture(ClientCalls.futureUnaryCall(call, build()))
                .handle(
                        (response, error) -> {
                            if (error != null || shouldRetry(response)) {
                                if (error != null && !(error instanceof StatusRuntimeException)) {
                                    // not a network failure, some other weirdness going on
                                    // just fail fast
                                    return CompletableFuture.<R>failedFuture(error);
                                }

                                // the query status has been identified as failing or otherwise
                                // needing a retry let's do this again after a delay
                                return Delayer.delayBackOff(attempt, client.executor)
                                        .thenCompose((v) -> executeAsync(client, attempt + 1));
                            }

                            // successful response from Hedera
                            return CompletableFuture.completedFuture(mapResponse(response));
                        })
                .thenCompose(x -> x);
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    public void executeAsync(Client client, BiConsumer<R, Throwable> callback) {
        executeAsync(client, DEFAULT_TIMEOUT, callback);
    }

    @SuppressWarnings({"FutureReturnValueIgnored", "InconsistentOverloads"})
    public void executeAsync(Client client, Duration timeout, BiConsumer<R, Throwable> callback) {
        executeAsync(client)
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .whenComplete(callback);
    }

    protected Query build() {
        onBuild(builder, headerBuilder.build());

        // TODO: Generate a payment transaction if one was not set and payment is required

        return builder.build();
    }

    /**
     * Called in {@link #build} just before the query is built. The intent is for the derived class
     * to assign their data variant to the query.
     */
    protected abstract void onBuild(Query.Builder queryBuilder, QueryHeader header);

    /**
     * Called in {@link #execute} just after receiving the query response from Hedera. By default it
     * triggers a retry when the pre-check status is {@code BUSY}.
     */
    protected boolean shouldRetry(Response response) {
        return Status.valueOf(getResponseHeader(response).getNodeTransactionPrecheckCode())
                == Status.Busy;
    }

    /** The derived class should access its response header and return. */
    protected abstract ResponseHeader getResponseHeader(Response response);

    /**
     * Called in {@link #execute} after receiving the query response from Hedera. The derived class
     * should map into its output type.
     */
    protected abstract R mapResponse(Response response);

    /**
     * Called in {@link #execute} to direct the invocation of the query to the appropriate gRPC
     * service.
     */
    protected abstract MethodDescriptor<Query, Response> getMethodDescriptor();
}
