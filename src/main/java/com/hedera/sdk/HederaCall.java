package com.hedera.sdk;

import com.google.protobuf.ByteString;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public abstract class HederaCall<Req, RawResp, Resp, E extends Throwable> {
    private @Nullable List<String> validationErrors;
    private final MapResponse<RawResp, Resp, E> mapResponse;

    // a single required constructor for subclasses to make it harder to forget
    protected HederaCall(MapResponse<RawResp, Resp, E> mapResponse) {
        this.mapResponse = mapResponse;
    }

    protected abstract io.grpc.MethodDescriptor<Req, RawResp> getMethod();

    public abstract Req toProto();

    protected abstract Channel getChannel();

    private ClientCall<Req, RawResp> newClientCall() {
        return getChannel().newCall(getMethod(), CallOptions.DEFAULT);
    }

    public final Resp execute() throws E {
        return mapResponse.mapResponse(ClientCalls.blockingUnaryCall(newClientCall(), toProto()));
    }

    public final void executeAsync(Function<Resp, Void> onSuccess, Function<Throwable, Void> onError) {
        ClientCalls.asyncUnaryCall(newClientCall(), toProto(), new ResponseObserver<>(mapResponse, onSuccess, onError));
    }

    public final Future<Resp> executeFuture() {
        var rawFuture = ClientCalls.futureUnaryCall(newClientCall(), toProto());

        return new Future<>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return rawFuture.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return rawFuture.isCancelled();
            }

            @Override
            public boolean isDone() {
                return rawFuture.isDone();
            }

            @Override
            public Resp get() throws InterruptedException, ExecutionException {
                var raw = rawFuture.get();

                try {
                    return mapResponse.mapResponse(raw);
                } catch (Throwable e) {
                    throw new ExecutionException(e);
                }
            }

            @Override
            public Resp get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                var raw = rawFuture.get(timeout, unit);

                try {
                    return mapResponse.mapResponse(raw);
                } catch (Throwable e) {
                    throw new ExecutionException(e);
                }
            }
        };
    }

    public abstract void validate();

    protected void addValidationError(String errMsg) {
        if (validationErrors == null)
            validationErrors = new ArrayList<>();
        validationErrors.add(errMsg);
    }

    protected void checkValidationErrors(String prologue) {
        if (validationErrors == null)
            return;
        var errors = validationErrors;
        validationErrors = null;
        throw new IllegalStateException(prologue + ":\n" + String.join("\n", errors));
    }

    protected final void require(boolean mustBeTrue, String errMsg) {
        if (!mustBeTrue) {
            addValidationError(errMsg);
        }
    }

    protected void require(@Nullable List setValue, String errMsg) {
        require(setValue != null && !setValue.isEmpty(), errMsg);
    }

    protected void require(@Nullable ByteString setValue, String errMsg) {
        require(setValue != null && !setValue.isEmpty(), errMsg);
    }

    protected void requireExactlyOne(String errMsg, String errCollision, boolean... values) {
        var oneIsTrue = false;

        for (var maybeTrue : values) {
            if (maybeTrue && oneIsTrue) {
                addValidationError(errCollision);
                return;
            }

            oneIsTrue |= maybeTrue;
        }

        if (!oneIsTrue) {
            addValidationError(errMsg);
        }
    }

    protected void require(@Nullable String setValue, String errMsg) {
        require(setValue != null && setValue.isEmpty(), errMsg);
    }

    // builder.isInitialized() is always true
    /* protected void require(@Nullable MessageOrBuilder setValue, String errMsg) {
     * if (setValue == null || !setValue.isInitialized()) {
     * addValidationError(errMsg);
     * }
     * } */

    private static class ResponseObserver<RawResp, Resp> implements StreamObserver<RawResp> {
        private final MapResponse<RawResp, Resp, ? extends Throwable> mapResponse;
        private final Function<Resp, Void> onResponse;
        private final Function<Throwable, Void> onError;
        private boolean callbackExecuted = false;

        private ResponseObserver(MapResponse<RawResp, Resp, ? extends Throwable> mapResponse, Function<Resp, Void> onResponse, Function<Throwable, Void> onError) {
            this.mapResponse = mapResponse;
            this.onResponse = onResponse;
            this.onError = onError;
        }

        @Override
        public void onNext(RawResp rawResp) {
            if (!callbackExecuted) {
                try {
                    var resp = mapResponse.mapResponse(rawResp);
                    callbackExecuted = true;
                    onResponse.apply(resp);
                } catch (Throwable e) {
                    onError(e);
                }
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

    @FunctionalInterface
    protected interface MapResponse<RawResp, Resp, E extends Throwable> {
        Resp mapResponse(RawResp rawResp) throws E;
    }
}
