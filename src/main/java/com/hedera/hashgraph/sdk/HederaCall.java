package com.hedera.hashgraph.sdk;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.ResponseCodeEnum;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.CallOptions;
import io.grpc.Channel;
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

    @Deprecated
    protected abstract Channel getChannel();

    protected abstract Channel getChannel(Client client);

    protected abstract Resp mapResponse(RawResp raw) throws HederaException;

    protected Duration getDefaultTimeout() {
        return Duration.ZERO;
    }

    protected boolean shouldRetry(HederaThrowable e) {
        return e instanceof HederaException && ((HederaException) e).responseCode == ResponseCodeEnum.BUSY;
    }

    public final Resp execute(Client client) throws HederaException, HederaNetworkException {
        return execute(client, getDefaultTimeout());
    }

    public Resp execute(Client client, Duration retryTimeout) throws HederaException, HederaNetworkException {
        if (isExecuted) {
            throw new IllegalStateException("call already executed");
        }
        isExecuted = true;

        // N.B. only QueryBuilder used onPreExecute() so instead it should just override this
        // method instead

        final Backoff.FallibleProducer<Resp, HederaException> tryProduce = () ->
            mapResponse(ClientCalls.blockingUnaryCall(getChannel(client).newCall(getMethod(), CallOptions.DEFAULT), toProto()));

        return new Backoff(RETRY_DELAY, retryTimeout)
            .tryWhile(this::shouldRetry, tryProduce);
    }

    /**
     * @deprecated use {@link #execute(Client)} instead.
     */
    @Deprecated
    public Resp execute() throws HederaException, HederaNetworkException {
        return execute(getDefaultTimeout());
    }

    /**
     * @deprecated use {@link #execute(Client, Duration)} instead.
     */
    @Deprecated
    public Resp execute(Duration retryTimeout) throws HederaException, HederaNetworkException {
        if (isExecuted) {
            throw new IllegalStateException("call already executed");
        }
        isExecuted = true;

        final Backoff.FallibleProducer<Resp, HederaException> tryProduce = () ->
            mapResponse(ClientCalls.blockingUnaryCall(getChannel().newCall(getMethod(), CallOptions.DEFAULT), toProto()));

        return new Backoff(RETRY_DELAY, retryTimeout)
            .tryWhile(this::shouldRetry, tryProduce);
    }

    public final void executeAsync(Client client, Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
        executeAsync(client, getDefaultTimeout(), onSuccess, onError);
    }

    public void executeAsync(Client client, Duration retryTimeout, Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
        if (isExecuted) {
            throw new IllegalStateException("call already executed");
        }
        isExecuted = true;

        final Consumer<Consumer<HederaThrowable>> executeCall = (onError2) -> ClientCalls.asyncUnaryCall(getChannel(client).newCall(getMethod(), CallOptions.DEFAULT), toProto(),
            new CallStreamObserver(onSuccess, onError2));

        new Backoff(RETRY_DELAY, retryTimeout)
            .asyncTryWhile(this::shouldRetry, executeCall, onError);
    }

    /**
     * @deprecated use {@link #executeAsync(Client, Consumer, Consumer)} instead.
     */
    @Deprecated
    public final void executeAsync(Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
        executeAsync(getDefaultTimeout(), onSuccess, onError);
    }

    /**
     * @deprecated use {@link #executeAsync(Client, Duration, Consumer, Consumer)} instead.
     */
    @Deprecated
    public void executeAsync(Duration retryTimeout, Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
        if (isExecuted) {
            throw new IllegalStateException("call already executed");
        }
        isExecuted = true;

        final Consumer<Consumer<HederaThrowable>> executeCall = (onError2) -> ClientCalls.asyncUnaryCall(getChannel().newCall(getMethod(), CallOptions.DEFAULT), toProto(),
            new CallStreamObserver(onSuccess, onError2));

        new Backoff(RETRY_DELAY, retryTimeout)
            .asyncTryWhile(this::shouldRetry, executeCall, onError);
    }

    /**
     * Equivalent to {@link #executeAsync(Consumer, Consumer)} but providing {@code this}
     * to the callback for additional context.
     *
     *
     */
    @Deprecated
    public final void executeAsync(BiConsumer<T, Resp> onSuccess, BiConsumer<T, HederaThrowable> onError) {
        executeAsync(getDefaultTimeout(), onSuccess, onError);
    }

    /**
     * Equivalent to {@link #executeAsync(Duration, Consumer, Consumer)} but providing {@code this}
     * to the callback for additional context.
     */
    public final void executeAsync(Duration timeout, BiConsumer<T, Resp> onSuccess, BiConsumer<T, HederaThrowable> onError) {
        //noinspection unchecked
        executeAsync(timeout, resp -> onSuccess.accept((T) this, resp), err -> onError.accept((T) this, err));
    }

    /**
     * @deprecated this method is being removed because it has limited utility for users;
     * most implementations cannot guarantee that passing validation means the transaction
     * or query will always succeed, so the method name is misleading at best.
     */
    @VisibleForTesting
    public final void validate() {
        localValidate();
    }

    protected abstract void localValidate();

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
