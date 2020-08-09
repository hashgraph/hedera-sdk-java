package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TransactionID;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicQuery;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public final class TopicMessageQuery {
    private final ConsensusTopicQuery.Builder builder;

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

    // TODO: Refactor into a base class when we add more mirror query types
    public SubscriptionHandle subscribe(
        Client client,
        Consumer<TopicMessage> onNext
    ) {
        ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call =
            client.getNextMirrorChannel().newCall(ConsensusServiceGrpc.getSubscribeTopicMethod(), CallOptions.DEFAULT);

        SubscriptionHandle subscriptionHandle = new SubscriptionHandle(() -> {
            call.cancel("unsubscribed", null);
        });

        makeStreamingCall(call, builder, onNext, 0, new Instant[]{null}, new ReentrantReadWriteLock());

        return subscriptionHandle;
    }

    private static void makeStreamingCall(
        ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call,
        ConsensusTopicQuery.Builder query,
        Consumer<TopicMessage> onNext,
        int attempt,
        // startTime must be `final` or `effectively final` to be used within closures.
        Instant[] startTime,
        ReentrantReadWriteLock startTimeLock
    ) {
        if (attempt > 10) {
            throw new Error("Failed to connect to mirror node");
        }

        HashMap<TransactionID, ArrayList<ConsensusTopicResponse>> pendingMessages = new HashMap<>();

        ClientCalls.asyncServerStreamingCall(call, query.build(), new StreamObserver<ConsensusTopicResponse>() {
            @Override
            public void onNext(ConsensusTopicResponse consensusTopicResponse) {
                Lock lock;

                if (!consensusTopicResponse.hasChunkInfo()) {
                    // short circuit for no chunks
                    var message = TopicMessage.ofSingle(consensusTopicResponse);
                    lock = startTimeLock.writeLock();
                    lock.lock();
                    try {
                        startTime[0] = message.consensusTimestamp;
                    } finally {
                        lock.unlock();
                    }
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
                    lock = startTimeLock.writeLock();
                    lock.lock();
                    try {
                        startTime[0] = message.consensusTimestamp;
                    } finally {
                        lock.unlock();
                    }
                    System.out.println("After lock chunked");
                    onNext.accept(message);
                }
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof StatusException) {
                    var status = (StatusException)t;

                    if (status.getStatus().equals(Status.NOT_FOUND) || status.getStatus().equals(Status.UNAVAILABLE)) {
                        // Cannot use `CompletableFuture<U>` here since this future is never polled
                        try {
                            Thread.sleep(250 * (long)Math.pow(2, attempt));
                        } catch (InterruptedException e) {
                            // Do nothing
                        }

                        var lock = startTimeLock.writeLock();
                        lock.lock();
                        try {
                            startTime[0] = startTime[0].plusNanos(1);
                        } finally {
                            lock.unlock();
                        }

                        makeStreamingCall(call, query, onNext, attempt + 1, startTime, startTimeLock);
                    }
                }
            }

            @Override
            public void onCompleted() {
                // Do nothing
            }
        });
    }
}
