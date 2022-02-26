package com.hedera.hashgraph.sdk;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import java8.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

abstract class MirrorQuery<T extends MirrorQuery, ProtoRequestT, ProtoResponseT, ResponseT, ExecuteResponseT> {
    protected static final Logger LOGGER = LoggerFactory.getLogger(MirrorQuery.class);

    protected int maxAttempts = 10;
    protected Duration minBackoff = Client.DEFAULT_MIN_BACKOFF;
    protected Duration maxBackoff = Client.DEFAULT_MAX_BACKOFF;
    protected Duration currentBackoff = minBackoff;
    protected AtomicLong counter = new AtomicLong();
    protected List<ResponseT> responses = new ArrayList<>();
    protected AtomicInteger attempt = new AtomicInteger();
    protected Boolean retry = true;
    protected ClientCall<ProtoRequestT, ProtoResponseT> call;
    protected SubscriptionHandle subscriptionHandle = new SubscriptionHandle();

    protected Runnable completionHandler = this::onComplete;
    protected BiConsumer<Throwable, ResponseT> errorHandler = this::onErrorHandler;
    protected Predicate<Throwable> retryHandler = this::shouldRetry;
    protected Consumer<ProtoResponseT> nextHandler = this::defaultNextHandler;
    protected Consumer<ResponseT> listener = this::defaultListener;

    MirrorQuery() {
    }

    public T setMaxBackoff(Duration maxBackoff) {
        if (maxBackoff == null || maxBackoff.toMillis() < 500L) {
            throw new IllegalArgumentException("maxBackoff must be at least 500 ms");
        }
        this.maxBackoff = maxBackoff;
        // noinspection unchunked
        return (T) this;
    }

    public T setMinBackoff(Duration maxBackoff) {
        if (maxBackoff == null || maxBackoff.toMillis() < 500L) {
            throw new IllegalArgumentException("maxBackoff must be at least 500 ms");
        }
        this.maxBackoff = maxBackoff;
        // noinspection unchunked
        return (T) this;
    }

    public T setMaxAttempts(int maxAttempts) {
        if (maxAttempts < 0) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        this.maxAttempts = maxAttempts;
        // noinspection unchunked
        return (T) this;
    }

    public T setRetryHandler(Predicate<Throwable> retryHandler) {
        this.retryHandler = Objects.requireNonNull(retryHandler, "retryHandler must not be null");
        // noinspection unchunked
        return (T) this;
    }


    public T setErrorHandler(BiConsumer<Throwable, ResponseT> errorHandler) {
        this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler must not be null");
        // noinspection unchunked
        return (T) this;
    }

    public T setCompletionHandler(Runnable completionHandler) {
        this.completionHandler = Objects.requireNonNull(completionHandler, "completionHandler must not be null");
        // noinspection unchunked
        return (T) this;
    }

    public T setListener(Consumer<ResponseT> listener) {
        this.listener = Objects.requireNonNull(listener, "completionHandler must not be null");
        // noinspection unchunked
        return (T) this;
    }

    /**
     * This method will retry the following scenarios:
     * <p>
     * UNAVAILABLE: Can occur when the mirror node's database or other downstream components are temporarily down.
     * <p>
     * RESOURCE_EXHAUSTED: Can occur when the mirror node's resources (database, threads, etc.) are temporarily exhausted.
     * <p>
     * INTERNAL: With a gRPC error status description that indicates the stream was reset. Stream resets can sometimes
     * occur when a proxy or load balancer disconnects the client.
     *
     * @param throwable the potentially retryable exception
     * @return if the request should be retried or not
     */
    protected boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException) {
            var statusRuntimeException = (StatusRuntimeException) throwable;
            var code = statusRuntimeException.getStatus().getCode();
            var description = statusRuntimeException.getStatus().getDescription();

            return (code == io.grpc.Status.Code.UNAVAILABLE) ||
                    (code == io.grpc.Status.Code.RESOURCE_EXHAUSTED) ||
                    (code == Status.Code.INTERNAL && description != null && Executable.RST_STREAM.matcher(description).matches());
        }

        return false;
    }

    protected void onComplete() {
        LOGGER.info("Subscription finished");
    }

    protected abstract MethodDescriptor<ProtoRequestT, ProtoResponseT> getMethodDescriptor();

    protected abstract ProtoRequestT makeRequest();

    @Nullable
    protected abstract ResponseT mapResponse(ProtoResponseT protoResponse);

    protected void defaultNextHandler(ProtoResponseT protoResponse) {
        counter.incrementAndGet();
        var response = mapResponse(protoResponse);
        if (response != null) {
            try {
                listener.accept(response);
            } catch (Throwable t) {
                errorHandler.accept(t, response);
            }
        }
    }

    protected void defaultListener(ResponseT response) {
        responses.add(response);
    }

    protected void onErrorHandler(Throwable t, ResponseT message) {
        logError(t);
    }

    protected void logError(Throwable t) {
        LOGGER.warn("Received streaming error during attempt #{}. Waiting {} before next attempt: {}",
                attempt.get(), currentBackoff.toString(), t.getMessage());
    }

    public ExecuteResponseT execute(Client client) {
        subscribe(client);
        while (retry) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return mapExecuteResponse();
    }

    protected abstract ExecuteResponseT mapExecuteResponse();

    CompletableFuture<ExecuteResponseT> executeAsyncInternal(Client client) {
        var delay =  CompletableFuture.runAsync(() -> {},
                CompletableFuture.delayedExecutor(currentBackoff.toMillis(), TimeUnit.MILLISECONDS, client.executor));
        if (retry) {
            return delay.thenCompose((v) -> executeAsyncInternal(client));
        }

        return CompletableFuture.completedFuture(mapExecuteResponse());
    }

    public CompletableFuture<ExecuteResponseT> executeAsync(Client client) {
        subscribe(client);
        return executeAsyncInternal(client);
    }

    public SubscriptionHandle subscribe(Client client) {
        client.executor.submit(() -> {
            try {
                call = client.mirrorNetwork.getNextMirrorNode().getChannel().newCall(getMethodDescriptor(), CallOptions.DEFAULT);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            subscriptionHandle.setOnUnsubscribe(() -> {
                call.cancel("unsubscribe", null);
            });

            var protoRequest = makeRequest();

            ClientCalls.asyncServerStreamingCall(call, protoRequest, new StreamObserver<>() {
                @Override
                public void onNext(ProtoResponseT response) {
                    nextHandler.accept(response);
                }

                @Override
                public void onError(Throwable t) {
                    retry = attempt.get() >= maxAttempts || !retryHandler.test(t);
                    if (retry) {
                        errorHandler.accept(t, null);
                        return;
                    }

                    currentBackoff = currentBackoff.multipliedBy(2);
                    attempt.incrementAndGet();
                    subscriptionHandle.unsubscribe();

                    CompletableFuture.delayedExecutor(currentBackoff.toMillis(), TimeUnit.MILLISECONDS, client.executor).execute(() -> {
                        subscribe(client);
                    });
                }

                @Override
                public void onCompleted() {
                    retry = false;
                    completionHandler.run();
                }
            });
        });

        return subscriptionHandle;
    }
}
