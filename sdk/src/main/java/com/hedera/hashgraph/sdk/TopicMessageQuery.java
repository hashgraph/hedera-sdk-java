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
import java8.util.function.Consumer;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

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

        makeStreamingCall(call, builder.build(), onNext, 0);

        return subscriptionHandle;
    }

    private static void makeStreamingCall(ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call, ConsensusTopicQuery query, Consumer<TopicMessage> onNext, int attempt) {
        if (attempt > 10) {
            throw new Error("Failed to connect to mirror node");
        }

        HashMap<TransactionID, ArrayList<ConsensusTopicResponse>> pendingMessages = new HashMap<>();

        ClientCalls.asyncServerStreamingCall(call, query, new StreamObserver<ConsensusTopicResponse>() {
            @Override
            public void onNext(ConsensusTopicResponse consensusTopicResponse) {
                if (!consensusTopicResponse.hasChunkInfo()) {
                    // short circuit for no chunks
                    onNext.accept(TopicMessage.ofSingle(consensusTopicResponse));
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
                    onNext.accept(TopicMessage.ofMany(chunks));
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

                        makeStreamingCall(call, query, onNext, attempt + 1);
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
