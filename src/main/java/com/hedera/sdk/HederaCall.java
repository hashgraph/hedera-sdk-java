package com.hedera.sdk;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.netty.shaded.io.netty.util.concurrent.GlobalEventExecutor;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public abstract class HederaCall<Req, RawResp, Resp> {
    protected abstract io.grpc.MethodDescriptor<Req, RawResp> getMethod();

    public abstract Req toProto();

    protected abstract Channel getChannel();

    protected abstract Resp mapResponse(RawResp raw) throws HederaException;

    private ClientCall<Req, RawResp> newClientCall() {
        return getChannel().newCall(getMethod(), CallOptions.DEFAULT);
    }

    public final Resp execute() throws HederaException {
        return mapResponse(ClientCalls.blockingUnaryCall(newClientCall(), toProto()));
    }

    public final void executeAsync(Consumer<Resp> onSuccess, Consumer<Throwable> onError) {
        ClientCalls.asyncUnaryCall(newClientCall(), toProto(), new StreamObserver<>() {
            @Override
            public void onNext(RawResp value) {
                try {
                    var response = mapResponse(value);
                    onSuccess.accept(response);
                } catch (HederaException e) {
                    onError.accept(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                onError.accept(t);
            }

            @Override
            public void onCompleted() {}
        });
    }


}
