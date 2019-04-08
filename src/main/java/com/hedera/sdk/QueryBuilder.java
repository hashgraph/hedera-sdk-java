package com.hedera.sdk;

import com.google.common.util.concurrent.Futures;
import com.hedera.sdk.proto.*;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.Future;
import java.util.function.Function;

public abstract class QueryBuilder<Resp> extends ValidatedBuilder {
    protected com.hedera.sdk.proto.Query.Builder inner = com.hedera.sdk.proto.Query.newBuilder();
    private final Function<Response, Resp> mapResponse;

    protected QueryBuilder(Function<Response, Resp> mapResponse) {
        this.mapResponse = mapResponse;
    }

    protected abstract QueryHeader.Builder getHeaderBuilder();

    protected abstract io.grpc.MethodDescriptor<com.hedera.sdk.proto.Query, Response> getMethod();

    private ClientCall<Query, Response> newClientCall(Client client) {
        return client.getChannel()
            .newCall(getMethod(), CallOptions.DEFAULT);
    }

    public final Resp execute(Client client) {
        return mapResponse.apply(ClientCalls.blockingUnaryCall(newClientCall(client), inner.build()));
    }

    public final void executeAsync(Client client, Function<Resp, Void> onSuccess, Function<Throwable, Void> onError) {
        ClientCalls.asyncUnaryCall(newClientCall(client), inner.build(), new ResponseObserver(mapResponse.andThen(onSuccess), onError));
    }

    public final Future<Resp> executeFuture(Client client) {
        return Futures.lazyTransform(ClientCalls.futureUnaryCall(newClientCall(client), inner.build()), mapResponse::apply);
    }

    public QueryBuilder setPayment(Transaction transaction) {
        getHeaderBuilder().setPayment(transaction.build());
        return this;
    }

    private static final class ResponseObserver implements StreamObserver<Response> {
        private final Function<Response, Void> onResponse;
        private final Function<Throwable, Void> onError;
        private boolean callbackExecuted = false;

        private ResponseObserver(Function<Response, Void> onResponse, Function<Throwable, Void> onError) {
            this.onResponse = onResponse;
            this.onError = onError;
        }

        @Override
        public void onNext(Response value) {
            if (!callbackExecuted) {
                callbackExecuted = true;
                onResponse.apply(value);
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!callbackExecuted) {
                callbackExecuted = true;
                onError.apply(t);
            }
        }

        @Override
        public void onCompleted() {}
    }

    protected abstract void doValidate();

    /** Check that the query was built properly, throwing an exception on any errors. */
    @Override
    public final void validate() {
        require(getHeaderBuilder().hasPayment(), ".setPayment() required");
        doValidate();
        checkValidationErrors("query builder failed validation");
    }
}
