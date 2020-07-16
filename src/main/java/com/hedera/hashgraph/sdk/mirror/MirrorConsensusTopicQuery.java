package com.hedera.hashgraph.sdk.mirror;

import com.hedera.hashgraph.proto.TransactionID;
import com.hedera.hashgraph.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicQuery;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicQueryOrBuilder;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MirrorConsensusTopicQuery {
    private ConsensusTopicQuery.Builder builder;

    public MirrorConsensusTopicQuery() {
        builder = ConsensusTopicQuery.newBuilder();
    }

    public MirrorConsensusTopicQuery setTopicId(ConsensusTopicId topicId) {
        builder.setTopicID(topicId.toProto());
        return this;
    }

    public MirrorConsensusTopicQuery setStartTime(Instant startTime) {
        builder.setConsensusStartTime(TimestampHelper.timestampFrom(startTime));
        return this;
    }

    public MirrorConsensusTopicQuery setEndTime(Instant endTime) {
        builder.setConsensusEndTime(TimestampHelper.timestampFrom(endTime));
        return this;
    }

    public MirrorConsensusTopicQuery setLimit(long limit) {
        builder.setLimit(limit);
        return this;
    }

    // TODO: Refactor into a base class when we add more mirror query types
    public MirrorSubscriptionHandle subscribe(
        MirrorClient mirrorClient,
        Consumer<MirrorConsensusTopicResponse> onNext,
        Consumer<Throwable> onError)
    {
        final ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call =
            mirrorClient.channel.newCall(ConsensusServiceGrpc.getSubscribeTopicMethod(), CallOptions.DEFAULT);

        final MirrorSubscriptionHandle subscriptionHandle = new MirrorSubscriptionHandle(() -> {
            call.cancel("unsubscribed", null);
        });

        final HashMap<TransactionID, ArrayList<ConsensusTopicResponse>> pendingMessages = new HashMap<>();

        ClientCalls.asyncServerStreamingCall(call, builder.build(), new StreamObserver<ConsensusTopicResponse>() {
            @Override
            public void onNext(ConsensusTopicResponse consensusTopicResponse) {
                if (!consensusTopicResponse.hasChunkInfo()) {
                    // short circuit for no chunks
                    onNext.accept(MirrorConsensusTopicResponse.ofSingle(consensusTopicResponse));
                    return;
                }

                // get the list of chunks for this pending message
                TransactionID initialTransactionID = consensusTopicResponse.getChunkInfo().getInitialTransactionID();
                pendingMessages.putIfAbsent(initialTransactionID, new ArrayList<>());
                ArrayList<ConsensusTopicResponse> chunks = pendingMessages.get(initialTransactionID);

                // not possible as we do [putIfAbsent]
                assert chunks != null;

                // add our response to the pending chunk list
                chunks.add(consensusTopicResponse);

                // if we now have enough chunks, emit
                if (chunks.size() == consensusTopicResponse.getChunkInfo().getTotal()) {
                    onNext.accept(MirrorConsensusTopicResponse.ofMany(chunks));
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
