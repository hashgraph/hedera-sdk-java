package com.hedera.sdk;

import com.google.common.util.concurrent.Futures;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;
import java.util.concurrent.Future;
import java.util.function.Function;

public abstract class HederaCall<Req, RawResp, Resp> {
    private final Function<RawResp, Resp> mapResponse;

    // a single required constructor for subclasses to make it harder to forget
    protected HederaCall(Function<RawResp, Resp> mapResponse) {
        this.mapResponse = mapResponse;
    }

    protected abstract io.grpc.MethodDescriptor<Req, RawResp> getMethod();

    protected abstract Req buildRequest();

    protected abstract Channel getChannel();

    private ClientCall<Req, RawResp> newClientCall() {
        return getChannel().newCall(getMethod(), CallOptions.DEFAULT);
    }

    public final Resp execute() {
        return mapResponse.apply(ClientCalls.blockingUnaryCall(newClientCall(), buildRequest()));
    }

    public final void executeAsync(Function<Resp, Void> onSuccess, Function<Throwable, Void> onError) {
        ClientCalls.asyncUnaryCall(newClientCall(), buildRequest(), new ResponseObserver<>(mapResponse.andThen(onSuccess), onError));
    }

    public final Future<Resp> executeFuture() {
        return Futures.lazyTransform(ClientCalls.futureUnaryCall(newClientCall(), buildRequest()), mapResponse::apply);
    }

    private static final class ResponseObserver<Resp> implements StreamObserver<Resp> {
        private final Function<Resp, Void> onResponse;
        private final Function<Throwable, Void> onError;
        private boolean callbackExecuted = false;

        private ResponseObserver(Function<Resp, Void> onResponse, Function<Throwable, Void> onError) {
            this.onResponse = onResponse;
            this.onError = onError;
        }

        @Override
        public void onNext(Resp value) {
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
}
