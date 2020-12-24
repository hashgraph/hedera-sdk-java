package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TransactionID;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicQuery;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public final class TopicMessageQuery {
    private final ConsensusTopicQuery.Builder builder;

    @Nullable
    private BiConsumer<Throwable, TopicMessage> errorHandler;

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

    public TopicMessageQuery setErrorHandler(BiConsumer<Throwable, TopicMessage> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    // TODO: Refactor into a base class when we add more mirror query types
    public SubscriptionHandle subscribe(
        Client client,
        Consumer<TopicMessage> onNext
    ) {
        SubscriptionHandle subscriptionHandle = new SubscriptionHandle();

        makeStreamingCall(client, subscriptionHandle, errorHandler, builder.build(), onNext, 0, new Instant[]{null});

        return subscriptionHandle;
    }

    private static void makeStreamingCall(
        Client client,
        SubscriptionHandle subscriptionHandle,
        @Nullable BiConsumer<Throwable, TopicMessage> errorHandler,
        ConsensusTopicQuery query,
        Consumer<TopicMessage> onNext,
        int attempt,
        // startTime must be `final` or `effectively final` to be used within closures.
        Instant[] startTime
    ) {
        if (attempt > 10) {
            if (errorHandler != null) {
                errorHandler.accept(new Error("Failed to connect to mirror node"), null);
                return;
            }
        }

        ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call =
            client.mirrorNetwork.getNextMirrorNode().getChannel().newCall(ConsensusServiceGrpc.getSubscribeTopicMethod(), CallOptions.DEFAULT);

        subscriptionHandle.setOnUnsubscribe(() -> {
            call.cancel("unsubscribe", null);
        });

        HashMap<TransactionID, ArrayList<ConsensusTopicResponse>> pendingMessages = new HashMap<>();
        ClientCalls.asyncServerStreamingCall(call, query, new StreamObserver<ConsensusTopicResponse>() {
            @Override
            public void onNext(ConsensusTopicResponse consensusTopicResponse) {
                if (!consensusTopicResponse.hasChunkInfo()) {
                    // short circuit for no chunks
                    var message = TopicMessage.ofSingle(consensusTopicResponse);
                    startTime[0] = message.consensusTimestamp;
                    try {
                        onNext.accept(message);
                    } catch (Throwable e) {
                        if (errorHandler != null) {
                            errorHandler.accept(e, message);
                        }
                    }
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
                    startTime[0] = message.consensusTimestamp;
                    try {
                        onNext.accept(message);
                    } catch (Throwable e) {
                        if (errorHandler != null) {
                            errorHandler.accept(e, null);
                        }
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof StatusRuntimeException) {
                    var status = (StatusRuntimeException) t;

                    if (
                        status.getStatus().getCode().equals(Status.NOT_FOUND.getCode()) ||
                            status.getStatus().getCode().equals(Status.UNAVAILABLE.getCode())
                    ) {
                        // Cannot use `CompletableFuture<U>` here since this future is never polled
                        try {
                            Thread.sleep(250 * (long) Math.pow(2, attempt));
                        } catch (InterruptedException e) {
                            // Do nothing
                        }

                        if (startTime[0] != null) {
                            startTime[0] = startTime[0].plusNanos(1);
                        }

                        call.cancel("unsubscribed", null);
                        makeStreamingCall(client, subscriptionHandle, errorHandler, query, onNext, attempt + 1, startTime);
                    }
                } else if (errorHandler != null) {
                    errorHandler.accept(t, null);
                }
            }

            @Override
            public void onCompleted() {
                // Do nothing
            }
        });
    }
}
