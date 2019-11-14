package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

public abstract class HederaCall<Req, RawResp, Resp, T extends HederaCall<Req, RawResp, Resp, T>> {
    private @Nullable
    List<String> validationErrors;

    private boolean isExecuted = false;

    private static final Duration RETRY_DELAY = Duration.ofMillis(500);

    protected abstract io.grpc.MethodDescriptor<Req, RawResp> getMethod();

    public abstract Req toProto();

    protected abstract Channel getChannel();

    protected abstract Resp mapResponse(RawResp raw) throws HederaException;

    protected Duration getDefaultTimeout() {
        return Duration.ZERO;
    }

    protected boolean shouldRetry(HederaThrowable e) {
        return e instanceof HederaException && ((HederaException) e).responseCode == ResponseCodeEnum.BUSY;
    }

    private ClientCall<Req, RawResp> newClientCall() {
        return getChannel().newCall(getMethod(), CallOptions.DEFAULT);
    }

    // callbacks which can perform their own calls
    protected void onPreExecute(Duration timeout) throws HederaException, HederaNetworkException { }

    protected void onPreExecuteAsync(Runnable onSuccess, Consumer<HederaThrowable> onError, Duration timeout) {
        onSuccess.run();
    }

    public final Resp execute() throws HederaException, HederaNetworkException {
        return execute(getDefaultTimeout());
    }

    public final Resp execute(Duration retryTimeout) throws HederaException, HederaNetworkException {
        if (isExecuted) {
            throw new IllegalStateException("call already executed");
        }
        isExecuted = true;

        Instant preStart = Instant.now();
        onPreExecute(retryTimeout);
        // take the time preExecute took into account so the given timeout holds
        Duration remaining = retryTimeout.minus(Duration.between(preStart, Instant.now()));

        final Backoff.FallibleProducer<Resp, HederaException> tryProduce = () ->
            mapResponse(ClientCalls.blockingUnaryCall(newClientCall(), toProto()));

        return new Backoff(RETRY_DELAY, remaining)
            .tryWhile(this::shouldRetry, tryProduce);
    }

    public final void executeAsync(Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
        executeAsync(onSuccess, onError, getDefaultTimeout());
    }

    public final void executeAsync(Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError, Duration retryTimeout) {
        if (isExecuted) {
            throw new IllegalStateException("call already executed");
        }
        isExecuted = true;

        final Consumer<Consumer<HederaThrowable>> executeCall = (onError2) ->
            ClientCalls.asyncUnaryCall(newClientCall(), toProto(),
                new CallStreamObserver(onSuccess, onError2));

        Instant preStart = Instant.now();
        onPreExecuteAsync(() ->
            // take the time preExecute took into account so the given timeout holds
            new Backoff(RETRY_DELAY, retryTimeout.minus(Duration.between(preStart, Instant.now())))
                .asyncTryWhile(this::shouldRetry, executeCall, onError), onError, retryTimeout);
    }

    /**
     * Equivalent to {@link #executeAsync(Consumer, Consumer)} but providing {@code this}
     * to the callback for additional context.
     */
    public final void executeAsync(BiConsumer<T, Resp> onSuccess, BiConsumer<T, HederaThrowable> onError) {
        executeAsync(onSuccess, onError, getDefaultTimeout());
    }

    /**
     * Equivalent to {@link #executeAsync(Consumer, Consumer, Duration)} but providing {@code this}
     * to the callback for additional context.
     */
    public final void executeAsync(BiConsumer<T, Resp> onSuccess, BiConsumer<T, HederaThrowable> onError, Duration timeout) {
        //noinspection unchecked
        executeAsync(resp -> onSuccess.accept((T) this, resp), err -> onError.accept((T) this, err), timeout);
    }

    public abstract void validate();

    protected void addValidationError(String errMsg) {
        if (validationErrors == null) validationErrors = new ArrayList<>();
        validationErrors.add(errMsg);
    }

    protected void checkValidationErrors(String prologue) {
        if (validationErrors == null) return;
        List<String> errors = validationErrors;
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
        boolean oneIsTrue = false;

        for (boolean maybeTrue : values) {
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
        require(setValue != null && !setValue.isEmpty(), errMsg);
    }

    private final class CallStreamObserver implements StreamObserver<RawResp> {

        private final Consumer<Resp> onSuccess;
        private final Consumer<HederaThrowable> onError;

        private volatile boolean onNextCalled = false;

        private CallStreamObserver(Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
            this.onSuccess = onSuccess;
            this.onError = onError;
        }

        @Override
        public void onNext(RawResp value) {
            if (onNextCalled) return;
            onNextCalled = true;

            try {
                Resp response = mapResponse(value);
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
        public void onCompleted() {
            // we don't care about this callback
        }
    }
}
