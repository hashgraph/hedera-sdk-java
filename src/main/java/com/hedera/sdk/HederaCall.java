package com.hedera.sdk;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class HederaCall<Req, RawResp, Resp> {
    private @Nullable List<String> validationErrors;

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
        ClientCalls.asyncUnaryCall(newClientCall(), toProto(), new ResponseObserver(onSuccess, onError));
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

    private class ResponseObserver implements StreamObserver<RawResp> {
        private final Consumer<Resp> onResponse;
        private final Consumer<Throwable> onError;
        private boolean callbackExecuted = false;

        private ResponseObserver(Consumer<Resp> onResponse, Consumer<Throwable> onError) {
            this.onResponse = onResponse;
            this.onError = onError;
        }

        @Override
        public void onNext(RawResp rawResp) {
            if (!callbackExecuted) {
                try {
                    var resp = mapResponse(rawResp);
                    callbackExecuted = true;
                    onResponse.accept(resp);
                } catch (Throwable e) {
                    onError(e);
                }
            }
        }

        @Override
        public void onError(Throwable t) {
            if (!callbackExecuted) {
                callbackExecuted = true;
                onError.accept(t);
            }
        }

        @Override
        public void onCompleted() {}
    }
}
