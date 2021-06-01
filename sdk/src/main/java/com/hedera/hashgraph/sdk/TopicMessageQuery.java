package com.hedera.hashgraph.sdk;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.hedera.hashgraph.sdk.proto.TransactionID;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicQuery;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;

public final class TopicMessageQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicMessageQuery.class);
    private static final Pattern RST_STREAM = Pattern
            .compile(".*(rst.stream.*internal.error|internal.error.*rst.stream).*",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private final ConsensusTopicQuery.Builder builder;
    private Runnable completionHandler = this::onComplete;
    private BiConsumer<Throwable, TopicMessage> errorHandler = this::onError;
    private int maxAttempts = 10;
    private Duration maxBackoff = Duration.ofSeconds(10L);
    private Predicate<Throwable> retryHandler = this::shouldRetry;

    public TopicMessageQuery() {
        builder = ConsensusTopicQuery.newBuilder();
    }

    public TopicMessageQuery setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProtobuf());
        return this;
    }

    public TopicMessageQuery setStartTime(Instant startTime) {
        builder.setConsensusStartTime(InstantConverter.toProtobuf(startTime));
        return this;
    }

    public TopicMessageQuery setEndTime(Instant endTime) {
        builder.setConsensusEndTime(InstantConverter.toProtobuf(endTime));
        return this;
    }

    public TopicMessageQuery setLimit(long limit) {
        builder.setLimit(limit);
        return this;
    }

    public TopicMessageQuery setCompletionHandler(Runnable completionHandler) {
        Objects.requireNonNull(completionHandler, "completionHandler must not be null");
        this.completionHandler = completionHandler;
        return this;
    }

    public TopicMessageQuery setErrorHandler(BiConsumer<Throwable, TopicMessage> errorHandler) {
        Objects.requireNonNull(errorHandler, "errorHandler must not be null");
        this.errorHandler = errorHandler;
        return this;
    }

    public TopicMessageQuery setMaxAttempts(int maxAttempts) {
        if (maxAttempts < 0) {
            throw new IllegalArgumentException("maxAttempts must be positive");
        }
        this.maxAttempts = maxAttempts;
        return this;
    }

    public TopicMessageQuery setMaxBackoff(Duration maxBackoff) {
        if (maxBackoff == null || maxBackoff.toMillis() < 500L) {
            throw new IllegalArgumentException("maxBackoff must be at least 500 ms");
        }
        this.maxBackoff = maxBackoff;
        return this;
    }

    public TopicMessageQuery setRetryHandler(Predicate<Throwable> retryHandler) {
        Objects.requireNonNull(retryHandler, "retryHandler must not be null");
        this.retryHandler = retryHandler;
        return this;
    }

    private void onComplete() {
        TopicId topicId = TopicId.fromProtobuf(builder.getTopicID());
        LOGGER.info("Subscription to topic {} complete", topicId);
    }

    private void onError(Throwable throwable, TopicMessage topicMessage) {
        TopicId topicId = TopicId.fromProtobuf(builder.getTopicID());
        LOGGER.error("Error attempting to subscribe to topic {}:", topicId, throwable);
    }

    /**
     * This method will retry the following scenarios:
     *
     * NOT_FOUND: Can occur when a client creates a topic and attempts to subscribe to it immediately before it is
     * creation shows up in the mirror node.
     *
     * UNAVAILABLE: Can occur when the mirror node's database or other downstream components are temporarily down.
     *
     * RESOURCE_EXHAUSTED: Can occur when the mirror node's resources are temporarily exhausted.
     *
     * INTERNAL: With details that indicate the stream was reset. Stream resets can sometimes occur when a proxy or
     * load balancer disconnected the client.
     *
     * @param throwable the potentially retryable exception
     * @return if the request should be retried or not
     */
    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException) {
            StatusRuntimeException statusRuntimeException = (StatusRuntimeException) throwable;
            Status.Code code = statusRuntimeException.getStatus().getCode();
            String description = statusRuntimeException.getStatus().getDescription();

            return code == Status.Code.NOT_FOUND ||
                    code == Status.Code.UNAVAILABLE ||
                    code == Status.Code.RESOURCE_EXHAUSTED ||
                    code == Status.Code.INTERNAL && RST_STREAM.matcher(description).matches();
        }

        return false;
    }

    // TODO: Refactor into a base class when we add more mirror query types
    public SubscriptionHandle subscribe(Client client, Consumer<TopicMessage> onNext) {
        SubscriptionHandle subscriptionHandle = new SubscriptionHandle();
        makeStreamingCall(client, subscriptionHandle, builder.build(), onNext, 0, new AtomicReference<>());
        return subscriptionHandle;
    }

    private void makeStreamingCall(
            Client client,
            SubscriptionHandle subscriptionHandle,
            ConsensusTopicQuery query,
            Consumer<TopicMessage> onNext,
            int attempt,
            AtomicReference<Instant> startTime
    ) {
        ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call =
                client.mirrorNetwork.getNextMirrorNode().getChannel()
                        .newCall(ConsensusServiceGrpc.getSubscribeTopicMethod(), CallOptions.DEFAULT);

        subscriptionHandle.setOnUnsubscribe(() -> {
            call.cancel("unsubscribe", null);
        });

        HashMap<TransactionID, ArrayList<ConsensusTopicResponse>> pendingMessages = new HashMap<>();
        ClientCalls.asyncServerStreamingCall(call, query, new StreamObserver<>() {
            @Override
            public void onNext(ConsensusTopicResponse consensusTopicResponse) {
                if (!consensusTopicResponse.hasChunkInfo()) {
                    // short circuit for no chunks
                    var message = TopicMessage.ofSingle(consensusTopicResponse);
                    startTime.set(message.consensusTimestamp);
                    onNext.accept(message);
                    return;
                }

                // get the list of chunks for this pending message
                var initialTransactionID = consensusTopicResponse.getChunkInfo().getInitialTransactionID();

                // Can't use `HashMap.putIfAbsent()` since that method is not available on Android
                if (!pendingMessages.containsKey(initialTransactionID)) {
                    pendingMessages.put(initialTransactionID, new ArrayList<>());
                }

                ArrayList<ConsensusTopicResponse> chunks = pendingMessages.get(initialTransactionID);

                // not possible as we do [putIfAbsent]
                // add our response to the pending chunk list
                Objects.requireNonNull(chunks).add(consensusTopicResponse);

                // if we now have enough chunks, emit
                if (chunks.size() == consensusTopicResponse.getChunkInfo().getTotal()) {
                    var message = TopicMessage.ofMany(chunks);
                    startTime.set(message.consensusTimestamp);
                    onNext.accept(message);
                }
            }

            @Override
            public void onError(Throwable t) {
                if (attempt >= maxAttempts || !retryHandler.test(t)) {
                    errorHandler.accept(t, null);
                    return;
                }

                long delay = Math.min(250 * (long) Math.pow(2, attempt), maxBackoff.toMillis());
                TopicId topicId = TopicId.fromProtobuf(query.getTopicID());
                LOGGER.warn("Error subscribing to topic {} during attempt #{}. Waiting {} ms before next attempt: {}",
                        topicId, attempt, delay, t.getMessage());
                call.cancel("unsubscribed", null);

                // Cannot use `CompletableFuture<U>` here since this future is never polled
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                startTime.getAndUpdate(s -> s != null ? s.plusNanos(1) : null);
                makeStreamingCall(client, subscriptionHandle, query, onNext, attempt + 1, startTime);
            }

            @Override
            public void onCompleted() {
                completionHandler.run();
            }
        });
    }
}
