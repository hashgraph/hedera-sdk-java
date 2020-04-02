package com.hedera.hashgraph.sdk;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.hedera.hashgraph.sdk.account.AccountId;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

public abstract class HederaCall<Req, RawResp, Resp, T extends HederaCall<Req, RawResp, Resp, T>> {
    private @Nullable
    List<String> validationErrors;

    private int attempt = 0;

    private static final Duration RETRY_DELAY = Duration.ofMillis(500);

    protected abstract io.grpc.MethodDescriptor<Req, RawResp> getMethod();

    @Internal
    public abstract Req toProto();

    protected abstract Channel getChannel(Client client);

    // Should return the node ID that was last used to execute
    protected abstract AccountId getNodeId();

    protected abstract Resp mapResponse(RawResp raw) throws HederaStatusException;

    protected Duration getDefaultTimeout() {
        return Duration.ZERO;
    }

    protected Optional<Duration> getNextDelay(Instant timeout) {
        attempt += 1;

        final Duration nextDelay = RETRY_DELAY.multipliedBy(
            ThreadLocalRandom.current().nextLong(1L << attempt));

        if (Instant.now().plus(nextDelay).isBefore(timeout)) {
            return Optional.of(nextDelay);
        } else {
            return Optional.empty();
        }
    }

    protected boolean shouldRetry(HederaThrowable e) {
        if (e instanceof HederaStatusException) {
            return ((HederaStatusException) e).status == Status.Busy;
        }

        if (e instanceof HederaNetworkException) {
            StatusRuntimeException cause = ((HederaNetworkException) e).cause;

            if (cause == null) {
                return false;
            }

            io.grpc.Status status = cause.getStatus();

            // retry with backoff if the node is temporarily unavailable
            return status.getCode() == io.grpc.Status.UNAVAILABLE.getCode() || status.getCode() == io.grpc.Status.RESOURCE_EXHAUSTED.getCode();
        }

        return false;
    }

    public final Resp execute(Client client) throws HederaStatusException, HederaNetworkException {
        return execute(client, getDefaultTimeout());
    }

    public Resp execute(Client client, Duration retryTimeout) throws HederaStatusException, HederaNetworkException, LocalValidationException {
        // Run local validator just before execute
        localValidate();

        // N.B. only QueryBuilder used onPreExecute() so instead it should just override this
        // method instead
        try {
            final Channel chan = getChannel(client);
            final ClientCall<Req, RawResp> call = chan.newCall(getMethod(), CallOptions.DEFAULT);
            final RawResp resp = ClientCalls.blockingUnaryCall(call, toProto());

            return mapResponse(resp);
        } catch (StatusRuntimeException e) {
            throw new HederaNetworkException(getNodeId(), e);
        }
    }

    public final void executeAsync(Client client, Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
        executeAsync(client, getDefaultTimeout(), onSuccess, onError);
    }

    public void executeAsync(Client client, Duration retryTimeout, Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
        // Run local validator just before execute
        localValidate();

        final Consumer<Consumer<HederaThrowable>> executeCall = (onError2) -> ClientCalls.asyncUnaryCall(getChannel(client).newCall(getMethod(), CallOptions.DEFAULT), toProto(),
            new CallStreamObserver(onSuccess, onError2));

        new Backoff(RETRY_DELAY, retryTimeout)
            .asyncTryWhile(this::shouldRetry, executeCall, onError);
    }

    @VisibleForTesting
    public final void validate() throws LocalValidationException {
        localValidate();
    }

    protected abstract void localValidate() throws LocalValidationException;

    protected void addValidationError(String errMsg) {
        if (validationErrors == null) validationErrors = new ArrayList<>();
        validationErrors.add(errMsg);
    }

    protected void checkValidationErrors(String prologue) {
        if (validationErrors == null) return;
        List<String> errors = validationErrors;
        validationErrors = null;
        throw new LocalValidationException(prologue + ":\n" + String.join("\n", errors));
    }

    protected final void require(boolean mustBeTrue, String errMsg) {
        if (!mustBeTrue) {
            addValidationError(errMsg);
        }
    }

    protected void require(@Nullable List<?> setValue, String errMsg) {
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
            } catch (HederaStatusException e) {
                onError.accept(e);
            }
        }

        @Override
        public void onError(Throwable t) {
            HederaThrowable exception;

            if (t instanceof StatusRuntimeException) {
                exception = new HederaNetworkException(getNodeId(), (StatusRuntimeException) t);
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
