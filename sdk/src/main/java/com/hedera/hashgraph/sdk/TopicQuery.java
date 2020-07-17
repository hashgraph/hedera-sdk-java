package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TransactionID;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicQuery;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import java8.util.function.Consumer;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public final class TopicQuery {
    private final ConsensusTopicQuery.Builder builder;

    public TopicQuery() {
        builder = ConsensusTopicQuery.newBuilder();
    }

    public TopicQuery setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProtobuf());
        return this;
    }

    public TopicQuery setStartTime(Instant startTime) {
        builder.setConsensusStartTime(InstantConverter.toProtobuf(startTime));
        return this;
    }

    public TopicQuery setEndTime(Instant endTime) {
        builder.setConsensusEndTime(InstantConverter.toProtobuf(endTime));
        return this;
    }

    public TopicQuery setLimit(long limit) {
        builder.setLimit(limit);
        return this;
    }

    // TODO: Refactor into a base class when we add more mirror query types
    public SubscriptionHandle subscribe(
        Client client,
        Consumer<TopicResponse> onNext,
        Consumer<Throwable> onError
    ) {
        ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call =
            client.getNextMirrorChannel().newCall(ConsensusServiceGrpc.getSubscribeTopicMethod(), CallOptions.DEFAULT);

        SubscriptionHandle subscriptionHandle = new SubscriptionHandle(() -> {
            call.cancel("unsubscribed", null);
        });

        HashMap<TransactionID, ArrayList<ConsensusTopicResponse>> pendingMessages = new HashMap<>();

        ClientCalls.asyncServerStreamingCall(call, builder.build(), new StreamObserver<ConsensusTopicResponse>() {
            @Override
            public void onNext(ConsensusTopicResponse consensusTopicResponse) {
                if (!consensusTopicResponse.hasChunkInfo()) {
                    // short circuit for no chunks
                    onNext.accept(TopicResponse.ofSingle(consensusTopicResponse));
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
                    onNext.accept(TopicResponse.ofMany(chunks));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                onError.accept(throwable);
            }

            @Override
            public void onCompleted() {
                // Do nothing
            }
        });

        return subscriptionHandle;
    }
}
