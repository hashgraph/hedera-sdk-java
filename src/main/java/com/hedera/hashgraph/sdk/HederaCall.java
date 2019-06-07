package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
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

    public final Resp execute() throws HederaException, HederaNetworkException {
        return mapResponse(ClientCalls.blockingUnaryCall(newClientCall(), toProto()));
    }

    public final void executeAsync(Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
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
                HederaThrowable exception;

                if (t instanceof StatusRuntimeException) {
                    exception = new HederaNetworkException((StatusRuntimeException) t);
                } else if (t instanceof HederaThrowable) {
                    exception = (HederaThrowable) t;
                } else {
                    throw new RuntimeException("unhandled exception type", t);
                }

                onError.accept(exception);
            }

            @Override
            public void onCompleted() { }
        });
    }

    protected abstract void validate();

    protected void addValidationError(String errMsg) {
        if (validationErrors == null) validationErrors = new ArrayList<>();
        validationErrors.add(errMsg);
    }

    protected void checkValidationErrors(String prologue) {
        if (validationErrors == null) return;
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
}
